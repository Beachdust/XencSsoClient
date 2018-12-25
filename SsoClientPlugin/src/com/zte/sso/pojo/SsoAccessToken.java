package com.zte.sso.pojo;

import com.alibaba.fastjson.annotation.JSONField;

public class SsoAccessToken {
	@JSONField(name = "access_token")
	private String accessToken;
	@JSONField(name = "token_type")
	private String tokenType;
	@JSONField(name = "expires_in")
	private String expiresIn;
	@JSONField(name = "refresh_token")
	private String refreshToken;
	private String username;
	@JSONField(name = "example_parameter")
	private String exampleParameter;

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public String getTokenType() {
		return tokenType;
	}

	public void setTokenType(String tokenType) {
		this.tokenType = tokenType;
	}

	public String getExpiresIn() {
		return expiresIn;
	}

	public void setExpiresIn(String expiresIn) {
		this.expiresIn = expiresIn;
	}

	public String getRefreshToken() {
		return refreshToken;
	}

	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getExampleParameter() {
		return exampleParameter;
	}

	public void setExampleParameter(String exampleParameter) {
		this.exampleParameter = exampleParameter;
	}
}
