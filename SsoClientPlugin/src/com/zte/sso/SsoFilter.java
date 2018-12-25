package com.zte.sso;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSON;
import com.zte.sso.httputil.HttpClientSSLUtil;
import com.zte.sso.httputil.HttpResult;
import com.zte.sso.pojo.SsoAccessToken;
import com.zte.sso.util.SsoCookieUtil;

public class SsoFilter implements Filter {
	/**
	 * 拦截访问第三方应用的所有请求，判断是否已登录统一认证平台
	 */
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest httpServletRequest = (HttpServletRequest) request;
		HttpServletResponse httpServletResponse = (HttpServletResponse) response;
		String requestURI = httpServletRequest.getRequestURI();
		/**
		 * 拦截统一登录平台发过来的请求
		 */
		if (requestURI != null && requestURI.indexOf(SSOParams.SSO_CALL_PATH_ROOT) >= 0) {
			SsoClientService.getInstance().doSsoClientAction(httpServletRequest, httpServletResponse);
			return;
		}
		/**
		 * 如果是第三方应用判断是已经登录以后的访问，直接进入过滤链，不用处理
		 */
		if (isLogin(httpServletRequest, httpServletResponse)) {
			chain.doFilter(request, response);
			return;
		}
		if (isCallbackPageRequest(httpServletRequest)) {
			SsoAccessToken tokenReponse = getTokenFromOes(httpServletRequest, httpServletResponse);
			System.out.println("[access_token] = " + JSON.toJSONString(tokenReponse));
			/**
			 * 如果使用code从OES获取到了token，说明用户在该客户端已经登录过OES，则在response中
			 * 加入带token和username的两个COOKIE，再重定向到原访问页面。<br/>
			 * 如果没有获取到token,说明该code无效，用户在该客户端没有登录过OES，或者登录已经失效，
			 * 则向下走，重定向到OES的登录页面。
			 */
			if (tokenReponse != null) {
				String accessToken = tokenReponse.getRefreshToken();
				if (accessToken != null && !"".equals(accessToken.trim())) {
					addCookieAndRedirectHomePage(httpServletResponse, httpServletRequest, tokenReponse);
					return;
				}
			}
		}
		redirectToLoginPage(httpServletRequest, httpServletResponse);
	}

	/**
	 * 处理对首页且带code（https://ip:port/homepage?code=xxxxxxx）的访问。<br/>
	 * 该请求是登录统一认证平台以后重定向过来的。<br/>
	 * 应该在<filter>标签中定义HOME_PAGE初始化参数，该参数为首页出去https://ip:port以后的URI
	 */
	private boolean isCallbackPageRequest(HttpServletRequest httpServletRequest) {
		String requestURI = httpServletRequest.getRequestURI();
		String code = httpServletRequest.getParameter("code");
		return requestURI != null && requestURI.indexOf(SSOParams.HOME_PAGE) >= 0 && code != null && !"".equals(code.trim());
	}

	/**
	 * 判断已经登录第三方应用<br/>
	 * 请求中带有用户已登录信息<br/>
	 * 本DEMO中使用COOKIE，因为要用OES必须开启COOKIE<br/>
	 * TODO
	 * 要对COOKIE做一下验证，最好在生成cookie的时候对HttpServletRequest对象做一个加密，再拿到这个cookie去验证
	 */
	private boolean isLogin(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
		String requestCookieToken = SsoCookieUtil.getRequestToken(httpServletRequest);
		String refreshedToken = TokenCache.getRefreshedToken(requestCookieToken);
		Cookie tokenCookie = new Cookie(SSOParams.COOKIE_TOKEN_KEY, refreshedToken);
		httpServletResponse.addCookie(tokenCookie);
		return refreshedToken != null;
	}

	/**
	 * 
	 */
	private void addCookieAndRedirectHomePage(HttpServletResponse httpServletResponse, HttpServletRequest httpServletRequest,
			SsoAccessToken tokenReponse) throws IOException {
		String token = tokenReponse.getRefreshToken();
		Cookie tokenCookie = new Cookie(SSOParams.COOKIE_TOKEN_KEY, token);
		Cookie userCookie = new Cookie(SSOParams.COOKIE_USERNAME_KEY, tokenReponse.getUsername());
		TokenCache.addToken(token);
		httpServletResponse.addCookie(tokenCookie);
		httpServletResponse.addCookie(userCookie);
		httpServletResponse.sendRedirect(SSOParams.HOME_PAGE);
	}

	/**
	 * 获取已登录统一认证平台的认证<br/>
	 * 没有登录会话 且 url请求参数中有code，使用该code向oes请求token<br/>
	 */
	private SsoAccessToken getTokenFromOes(HttpServletRequest httpServletRequest, ServletResponse response) {
		String code = httpServletRequest.getParameter("code");
		String url = new StringBuilder(SSOParams.SSO_URL).append("/api/oauth2/v1/authcode/access_token").append("?code=").append(code)
				.append("&grant_type=authorization_code&redirect_uri=").append(getCallbackUrl()).append("&client_id=")
				.append(SSOParams.CLIENT_ID).append("&client_secret=").append(SSOParams.CLIENT_SECRET).toString();
		HttpResult tokenResponse = HttpClientSSLUtil.doPost(url, null);
		try {
			return JSON.parseObject(tokenResponse.getResponseContent(), SsoAccessToken.class);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 没有登录会话 且 url请求参数中没有code ，在response中设置网页重定向到统一认证平台的登录页面
	 */
	private void redirectToLoginPage(HttpServletRequest httpServletRequest, ServletResponse response)
			throws IOException, ServletException {
		String params = new StringBuilder().append("?scope=user.role&response_type=code&state=0&redirect_uri=")
				.append(getCallbackUrl()).append("&client_id=").append(SSOParams.CLIENT_ID).toString();
		String redictUrl = new StringBuilder(SSOParams.SSO_URL).append("/api/oauth2/v1/authorize").append(params).toString();
		((HttpServletResponse) response).sendRedirect(redictUrl);
	}

	/**
	 * 获取回调到第三方应用首页的url
	 */
	private String getCallbackUrl() {
		String callbackUrl = SSOParams.LOCALHOST + SSOParams.HOME_PAGE;
		try {
			return URLEncoder.encode(callbackUrl, "utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return callbackUrl;
		}
	}

	@Override
	public void init(FilterConfig config) throws ServletException {
		SSOParams.CLIENT_ID = config.getInitParameter("CLIENT_ID");
		SSOParams.CLIENT_SECRET = config.getInitParameter("CLIENT_SECRET");
		SSOParams.LOCALHOST = config.getInitParameter("LOCALHOST");
		SSOParams.SSO_URL = config.getInitParameter("SSO_URL");
		// 用户下发同步实现类
		setSsoParam(SSOParams.INTERFACE_CLASS_NAME, config, "INTERFACE_CLASS_NAME");
		// 登录了以后重定向的页面地址
		SSOParams.HOME_PAGE = config.getInitParameter("HOME_PAGE");
		SSOParams.SSO_CALL_PATH_ROOT = config.getInitParameter("SSO_CALL_PATH_ROOT");
		SSOParams.SSO_LOGOUT = config.getInitParameter("SSO_LOGOUT");
		SSOParams.SSO_SYNC_USER = config.getInitParameter("SSO_SYNC_USER");
		setSsoParam(SSOParams.SSO_SYNC_ALL, config, "SSO_SYNC_ALL");
		setSsoParam(SSOParams.SSO_SYNC_ORG, config, "SSO_SYNC_ORG");
		// 自定义token在cookie中的key，以免冲突,默认为access_token
		setSsoParam(SSOParams.COOKIE_TOKEN_KEY, config, "COOKIE_TOKEN_KEY");
		// 自定义username在cookie中的key，以免冲突,默认为username
		setSsoParam(SSOParams.COOKIE_USERNAME_KEY, config, "COOKIE_USERNAME_KEY");
	}

	private void setSsoParam(String param, FilterConfig config, String key) {
		String initParameter = config.getInitParameter(key);
		if (initParameter != null && !"".equals(initParameter.trim())) {
			param = initParameter;
		}
	}

	@Override
	public void destroy() {
	}
}
