package com.zte.sso.httputil;

public class HttpResult {
	private int responseCode;
	private String responseContent;

	public HttpResult(int responseCode, String responseContent) {
		super();
		this.responseCode = responseCode;
		this.responseContent = responseContent;
	}

	public int getResponseCode() {
		return responseCode;
	}

	public void setResponseCode(int responseCode) {
		this.responseCode = responseCode;
	}

	public String getResponseContent() {
		return responseContent;
	}

	public void setResponseContent(String responseContent) {
		this.responseContent = responseContent;
	}
}
