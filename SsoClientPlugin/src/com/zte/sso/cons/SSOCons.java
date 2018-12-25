package com.zte.sso.cons;

public interface SSOCons {
	// oes的token刷新时间是900秒，这里设置刷新时间为比oes少一点
	long TOKEN_CACHE_TIME = 750000;
}
