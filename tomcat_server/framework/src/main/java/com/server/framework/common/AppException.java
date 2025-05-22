package com.server.framework.common;

public class AppException extends Exception
{
	String code;
	String message;
	public AppException(String code, String message)
	{
		super(message);
		this.code = code;
		this.message = message;
	}

	public AppException(String message)
	{
		super(message);
		this.code = "error";
		this.message = message;
	}

	public String getCode()
	{
		return code;
	}

	@Override public String getMessage()
	{
		return message;
	}
}
