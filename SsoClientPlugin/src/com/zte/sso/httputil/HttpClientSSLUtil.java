package com.zte.sso.httputil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

/*
 * 利用HttpClient进行post请求的工具类
 */
public class HttpClientSSLUtil {
	public static HttpResult doPost(String url, Map<String, String> map) {
		HttpResult httpResult = null;
		HttpClient httpClient = null;
		HttpPost httpPost = null;
		try {
			httpClient = new SSLClient();
			httpPost = new HttpPost(url);
			// 设置参数
			if (map != null) {
				List<NameValuePair> list = new ArrayList<NameValuePair>();
				Iterator<Entry<String, String>> iterator = map.entrySet().iterator();
				while (iterator.hasNext()) {
					Entry<String, String> elem = iterator.next();
					list.add(new BasicNameValuePair(elem.getKey(), elem.getValue()));
				}
				if (list.size() > 0) {
					UrlEncodedFormEntity entity = new UrlEncodedFormEntity(list, "utf-8");
					httpPost.setEntity(entity);
				}
			}
			HttpResponse response = httpClient.execute(httpPost);
			if (response != null) {
				return new HttpResult(response.getStatusLine().getStatusCode(), EntityUtils.toString(response.getEntity(), "UTF-8"));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return httpResult;
	}
}