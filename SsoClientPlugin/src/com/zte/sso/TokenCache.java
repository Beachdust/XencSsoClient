package com.zte.sso;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.alibaba.fastjson.JSON;
import com.zte.sso.cons.SSOCons;
import com.zte.sso.httputil.HttpClientSSLUtil;
import com.zte.sso.httputil.HttpResult;
import com.zte.sso.pojo.SsoAccessToken;
import com.zte.sso.pojo.Token;

/**
 * tokenTimeMap为缓存token的Map。<br/>
 * key为最新的token，value记录了获取到token的时间戳。<br/>
 * 有一条线程，每隔一秒去查看这些token在第三方应用中的时间是否超过定义的缓存时间。<br/>
 * 如果超过缓存时间，就把这一条token在缓存中移除。<br/>
 * 这样COOKIE中带来的token在tokenTimeMap不存在就刷新token,刷新不到token认为已经登录过期。<br/>
 * 如果COOKIE中带来的token在tokenTimeMap超过了缓存时间的一半，<br/>
 * 则请求统一认证平台，刷新token,把老的token移除，新的token放入tokenTimeMap。<br/>
 */
public class TokenCache {
	private static final int NEVER_REFRESHED = 0;
	private static Executor executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
	static {
		new Thread(new Runnable() {
			@Override
			public void run() {
				long i = 0;
				while (true) {
					System.out.println("[" + i++ + "]" + tokens);
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					Set<Entry<String, Token>> entrySet = tokens.entrySet();
					for (Iterator<Entry<String, Token>> iterator = entrySet.iterator(); iterator.hasNext();) {
						Entry<String, Token> entry = iterator.next();
						String key = entry.getKey();
						Token value = entry.getValue();
						if (System.currentTimeMillis() - value.getAddTime() >= SSOCons.TOKEN_CACHE_TIME) {
							tokens.remove(key);
						}
						if (value.getRefreshTime() == NEVER_REFRESHED
								&& value.getLastUseTime() - value.getAddTime() > SSOCons.TOKEN_CACHE_TIME * 2 / 3) {
							undateTokenRefreshToken(key, value);
						}
					}
				}
			}

			private void undateTokenRefreshToken(final String key, final Token token) {
				executor.execute(new Runnable() {
					@Override
					public void run() {
						token.setRefreshTime(System.currentTimeMillis());
						token.setRefreshToken(requestRefreshedToken(key));
					}
				});
			}
		}, "Thread[sso_client_check_token]").start();
	}
	private static Map<String, Token> tokens = new ConcurrentHashMap<String, Token>();

	public static String getRefreshedToken(String token) {
		if (token != null) {
			if (tokens.containsKey(token)) {
				// token被获取了一次，更新它的最新使用时间
				Token usingToken = tokens.get(token);
				usingToken.setLastUseTime(System.currentTimeMillis());
				String refreshToken = usingToken.getRefreshToken();
				// 被token check线程更新过
				if (!token.equals(refreshToken)) {
					synchronized (tokens) {
						usingToken.setAddTime(usingToken.getRefreshTime());
						usingToken.setRefreshTime(NEVER_REFRESHED);
						tokens.put(refreshToken, usingToken);
						tokens.remove(token);
					}
				}
				return token;
			} else {
				String newToken = requestRefreshedToken(token);
				// 请求到了refreshtoken,说明缓存里清除了，但是OES会话还没有结束，要还给客户端一个新的token
				if (newToken != null && !"".equals(newToken)) {
					synchronized (tokens) {
						long currentTimeMillis = System.currentTimeMillis();
						tokens.remove(token);
						tokens.put(newToken, new Token(currentTimeMillis, currentTimeMillis, newToken));
					}
					return newToken;
				}
			}
		}
		return null;
	}

	/**
	 * 请求oes获取新token
	 */
	private static String requestRefreshedToken(String token) {
		String url = new StringBuilder(SSOParams.SSO_URL)
				.append("/api/oauth2/v1/refreshToken?grant_type=authorization_code&refresh_token=").append(token).append("&client_id=")
				.append(SSOParams.CLIENT_ID).append("&client_secret=").append(SSOParams.CLIENT_SECRET).toString();
		HttpResult result = HttpClientSSLUtil.doPost(url, null);
		System.out.println("[REFRESH TOKEN] url = " + url + "  result: " + JSON.toJSONString(result));
		if (result != null) {
			int responseCode = result.getResponseCode();
			String responseContent = result.getResponseContent();
			if (responseCode > 199 && responseCode < 301 && responseContent != null) {
				SsoAccessToken tokenResp = JSON.parseObject(responseContent, SsoAccessToken.class);
				String refreshToken = tokenResp.getRefreshToken();
				if (refreshToken != null && !"".equals(refreshToken.trim())) {
					return refreshToken;
				}
			}
		}
		return null;
	}

	public static void addToken(String token) {
		long currentTimeMillis = System.currentTimeMillis();
		tokens.put(token, new Token(currentTimeMillis, currentTimeMillis, token));
	}

	public static void removeToken(String token) {
		tokens.remove(token);
	}
}
