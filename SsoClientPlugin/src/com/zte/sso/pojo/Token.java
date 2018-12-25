package com.zte.sso.pojo;

import com.alibaba.fastjson.JSON;

public class Token {
	long addTime;
	long lastUseTime;
	long refreshTime;
	String refreshToken;

	public Token(long addTime, long lastUseTime, String refreshToken) {
		this.addTime = addTime;
		this.lastUseTime = lastUseTime;
		this.refreshToken = refreshToken;
	}

	public long getAddTime() {
		return addTime;
	}

	public void setAddTime(long addTime) {
		this.addTime = addTime;
	}

	public long getLastUseTime() {
		return lastUseTime;
	}

	public void setLastUseTime(long lastUseTime) {
		this.lastUseTime = lastUseTime;
	}

	public String getRefreshToken() {
		return refreshToken;
	}

	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}

	public long getRefreshTime() {
		return refreshTime;
	}

	public void setRefreshTime(long refreshTime) {
		this.refreshTime = refreshTime;
	}

	@Override
	public String toString() {
		return JSON.toJSONString(this);
	}
}