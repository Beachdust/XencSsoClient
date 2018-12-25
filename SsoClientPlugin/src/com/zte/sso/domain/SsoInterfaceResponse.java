package com.zte.sso.domain;

import java.io.Serializable;

public class SsoInterfaceResponse implements Serializable {
	private static final long serialVersionUID = -4549129752408637705L;
	private int returnCode = 0;
	private String message;
	private boolean success = false;

	public int getReturnCode() {
		return returnCode;
	}

	public void setReturnCode(int returnCode) {
		this.returnCode = returnCode;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}
}
