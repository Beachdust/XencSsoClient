package com.zte.sso.util;

import javax.servlet.ServletRequest;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import com.zte.sso.cons.SSOCons;

public class SsoCookieUtil {
	private SsoCookieUtil() {
	}

	public static String getRequestToken(ServletRequest request) {
		return getRequestCookieValueByName(request, SSOCons.COOKIE_TOKEN);
	}

	public static String getRequestUserName(ServletRequest request) {
		return getRequestCookieValueByName(request, SSOCons.COOKIE_USERNAME);
	}

	private static String getRequestCookieValueByName(ServletRequest request, String cookieName) {
		Cookie[] cookies = ((HttpServletRequest) request).getCookies();
		if (cookies != null) {
			for (int i = 0; i < cookies.length; i++) {
				String name = cookies[i].getName();
				if (name != null && cookieName.equals(name.toLowerCase())) {
					return cookies[i].getValue();
				}
			}
		}
		return null;
	}
}
