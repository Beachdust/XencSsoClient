package com.zte.sso.cons;

public interface SSOCons {
	String COOKIE_TOKEN = "access_token";
	String COOKIE_USERNAME = "username";
	// oes的token刷新时间是900秒，这里设置刷新时间为比oes少一点
	long TOKEN_CACHE_TIME = 750000;
}
