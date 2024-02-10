package com.server.security.http;

import java.io.InputStream;
import java.util.Map;

public class HttpResponse
{
	String contentType;
	int status;
	Map<String, String> responseHeaders;
	InputStream inputStream;

	public String getContentType()
	{
		return contentType;
	}

	public void setContentType(String contentType)
	{
		this.contentType = contentType;
	}

	public int getStatus()
	{
		return status;
	}

	public void setStatus(int status)
	{
		this.status = status;
	}

	public Map<String, String> getResponseHeaders()
	{
		return responseHeaders;
	}

	public void setResponseHeaders(Map<String, String> responseHeaders)
	{
		this.responseHeaders = responseHeaders;
	}

	public InputStream getInputStream()
	{
		return inputStream;
	}

	public void setInputStream(InputStream inputStream)
	{
		this.inputStream = inputStream;
	}
}
