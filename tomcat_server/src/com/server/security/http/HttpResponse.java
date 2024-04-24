package com.server.security.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.apache.commons.io.IOUtils;

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

	public String getStringResponse() throws IOException
	{
		StringWriter stringWriter = new StringWriter();
		IOUtils.copy(getInputStream(), stringWriter, StandardCharsets.UTF_8);
		return stringWriter.toString();
	}

	public void setInputStream(InputStream inputStream)
	{
		this.inputStream = inputStream;
	}
}
