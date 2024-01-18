package com.server.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.HttpGet;

public class HttpAPI
{

	public static void copyInputStreamToOutputStream(InputStream inputStream, OutputStream outputStream) throws IOException
	{
		byte[] bytes = new byte[1024];
		int len = -1;

		while((len = inputStream.read(bytes)) != -1)
		{
			outputStream.write(bytes, 0, len);
		}
		outputStream.flush();
	}

	public static HttpResponse makeNetworkCall(String url, String method, String queryString, Map<String, String> headersMap, InputStream inputStream) throws IOException
	{
		if(StringUtils.isNotEmpty(queryString))
		{
			url = url.concat("?").concat(queryString);
		}

		HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(url).openConnection();
		httpURLConnection.setRequestMethod(method);
		httpURLConnection.setConnectTimeout(5000);

		for(Map.Entry<String, String> headers : headersMap.entrySet())
		{
			httpURLConnection.setRequestProperty(headers.getKey(), headers.getValue());
		}

		httpURLConnection.setDoInput(true);

		if(inputStream.available() > 0 && !method.equals(HttpGet.METHOD_NAME))
		{
			httpURLConnection.setDoOutput(true);
			copyInputStreamToOutputStream(inputStream, httpURLConnection.getOutputStream());
		}

		Map<String, String> responseHeadersMap = new HashMap<>();
		Set<String> keySet = httpURLConnection.getHeaderFields().keySet();
		for(String headerName : keySet)
		{
			responseHeadersMap.put(headerName, httpURLConnection.getHeaderField(headerName));
		}

		HttpResponse httpResponse = new HttpResponse();
		httpResponse.setResponseHeaders(responseHeadersMap);
		httpResponse.setContentType(httpURLConnection.getContentType());
		httpResponse.setStatus(httpURLConnection.getResponseCode());
		httpResponse.setInputStream(getStream(httpURLConnection));

		return httpResponse;
	}

	private static InputStream getStream(HttpURLConnection httpURLConnection)
	{
		try
		{
			return httpURLConnection.getInputStream();
		}
		catch(IOException e)
		{
			return httpURLConnection.getErrorStream();
		}
	}
}
