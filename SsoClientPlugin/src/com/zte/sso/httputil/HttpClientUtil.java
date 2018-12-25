package com.zte.sso.httputil;

import java.io.IOException;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

/**
 * http 瀹㈡埛绔伐鍏�
 * 
 * @author Tang
 *
 */
public class HttpClientUtil {
	public static String doGet(String url) {
		return doGet(url, null);
	}

	public static String doPost(String url) {
		return doPost(url, null);
	}

	public static String doGet(String url, Map<String, String> param) {
		String resultString = "";
		CloseableHttpResponse response = null;
		CloseableHttpClient httpClient = null;
		try {
			// 鍒涘缓Httpclient瀵硅�?
			httpClient = getHttpClient();
			// 鍒涘缓uri
			URIBuilder builder = new URIBuilder(url);
			if (param != null) {
				for (String key : param.keySet()) {
					builder.addParameter(key, param.get(key));
				}
			}
			URI uri = builder.build();
			// 鍒涘缓http GET璇锋�?
			HttpGet httpGet = new HttpGet(uri);
			RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(2000).setSocketTimeout(5000).build();
			httpGet.setConfig(requestConfig);
			// 鎵ц璇锋�?
			response = httpClient.execute(httpGet);
			// 鍒ゆ柇杩斿洖鐘舵�佹槸鍚︿负200
			if (response.getStatusLine().getStatusCode() == 200) {
				resultString = EntityUtils.toString(response.getEntity(), "UTF-8");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (response != null) {
					response.close();
				}
				httpClient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return resultString;
	}

	public static String doPost(String url, Map<String, String> param) {
		// 鍒涘缓Httpclient瀵硅�?
		CloseableHttpClient httpClient = null;
		CloseableHttpResponse response = null;
		String resultString = "";
		try {
			httpClient = getHttpClient();
			// 鍒涘缓Http Post璇锋�?
			HttpPost httpPost = new HttpPost(url);
			RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(2000).setSocketTimeout(5000).build();
			httpPost.setConfig(requestConfig);
			// 鍒涘缓鍙傛暟鍒楄�?
			if (param != null) {
				List<NameValuePair> paramList = new ArrayList<NameValuePair>();
				for (String key : param.keySet()) {
					paramList.add(new BasicNameValuePair(key, param.get(key)));
				}
				// 妯℃嫙琛ㄥ崟
				UrlEncodedFormEntity entity = new UrlEncodedFormEntity(paramList);
				httpPost.setEntity(entity);
			}
			// 鎵цhttp璇锋�?
			response = httpClient.execute(httpPost);
			resultString = EntityUtils.toString(response.getEntity(), "utf-8");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				response.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return resultString;
	}

	public static String doPostJson(String url, String json) {
		return doPostJson(url, json, null);
	}

	public static String doPostJson(String url, String json, Map<String, String> headers) {
		// 鍒涘缓Httpclient瀵硅�?
		CloseableHttpClient httpClient = null;
		CloseableHttpResponse response = null;
		String resultString = "";
		try {
			httpClient = getHttpClient();
			// 鍒涘缓Http Post璇锋�?
			HttpPost httpPost = new HttpPost(url);
			RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(10000).setSocketTimeout(10000).build();
			httpPost.setConfig(requestConfig);
			// 鍒涘缓璇锋眰鍐呭�?
			StringEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);
			httpPost.setEntity(entity);
			if (headers != null) {
				for (Map.Entry<String, String> entry : headers.entrySet()) {
					httpPost.setHeader(entry.getKey(), entry.getValue());
				}
			}
			// 鎵цhttp璇锋�?
			response = httpClient.execute(httpPost);
			resultString = EntityUtils.toString(response.getEntity(), "utf-8");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				response.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return resultString;
	}

	/**
	 * 鎻忚�?: https鍦板潃缁曡繃楠岃�? <br>
	 * 
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws KeyManagementException
	 *             <br>
	 * @author liuzg<br>
	 *         date 2018骞�7鏈�24鏃�<br>
	 */
	private static SSLContext createIgnoreVerifySSL() throws NoSuchAlgorithmException, KeyManagementException {
		SSLContext sc = SSLContext.getInstance("TLSv1.2");
		// 瀹炵幇涓�涓�?509TrustManager鎺ュ彛锛�?敤浜庣粫杩囬獙璇侊紝涓嶇敤淇敼閲岄潰鐨勬柟娉�
		X509TrustManager trustManager = new X509TrustManager() {
			@Override
			public void checkClientTrusted(java.security.cert.X509Certificate[] paramArrayOfX509Certificate, String paramString)
					throws CertificateException {
			}

			@Override
			public void checkServerTrusted(java.security.cert.X509Certificate[] paramArrayOfX509Certificate, String paramString)
					throws CertificateException {
			}

			@Override
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return null;
			}
		};
		sc.init(null, new TrustManager[] { trustManager }, null);
		return sc;
	}

	/**
	 * 鎻忚�?: 鑾峰彇httpClient瀵硅�? 锛屾敮鎸乭ttps <br>
	 * 
	 * @return
	 * @throws KeyManagementException
	 * @throws NoSuchAlgorithmException
	 *             <br>
	 * @author liuzg<br>
	 *         date 2018骞�7鏈�24鏃�<br>
	 */
	private static CloseableHttpClient getHttpClient() throws KeyManagementException, NoSuchAlgorithmException {
		// 閲囩敤缁曡繃楠岃瘉鐨勬柟寮忓鐞唄ttps璇锋�?
		SSLContext sslcontext = createIgnoreVerifySSL();
		// 璁剧疆鍗忚http鍜宧ttps瀵瑰簲鐨勫鐞唖ocket閾炬帴宸ュ巶鐨勫璞�?
		Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
				.register("http", PlainConnectionSocketFactory.INSTANCE).register("https", new SSLConnectionSocketFactory(sslcontext))
				.build();
		PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
		HttpClients.custom().setConnectionManager(connManager);
		// 鍒涘缓鑷畾涔夌殑httpclient瀵硅�?
		return HttpClients.custom().setConnectionManager(connManager).build();
	}
}
