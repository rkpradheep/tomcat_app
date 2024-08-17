package com.server.framework.http;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONObject;

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

	public static HttpResponse makeNetworkCall(String url, String method) throws IOException
	{
		return makeNetworkCall(url, method, null, null, null, null);
	}

	public static HttpResponse makeNetworkCall(String url, String method, Map<String, String> headersMap) throws IOException
	{
		return makeNetworkCall(url, method, null, headersMap, null, null);
	}


	public static HttpResponse makeNetworkCall(String url, String method, Map<String, String> headersMap, JSONObject jsonObject) throws IOException
	{
		headersMap.put("Content-Type", "application/json");
		return makeNetworkCall(url, method, null, headersMap, new ByteArrayInputStream(jsonObject.toString().getBytes()), null);
	}
	public static HttpResponse makeNetworkCall(String url, String method, Map<String, String> headersMap, Map<String, String> parametersMap) throws IOException
	{
		List<String> queryStringList = parametersMap.entrySet().stream().map(entrySet-> entrySet.getKey().concat("=").concat(entrySet.getValue())).collect(Collectors.toList());
		return makeNetworkCall(url, method, String.join("&", queryStringList), headersMap, null, null);
	}

	public static HttpResponse makeNetworkCall(String url, String method, String queryString, Map<String, String> headersMap, InputStream inputStream, Proxy proxy) throws IOException
	{
		if(StringUtils.isNotEmpty(queryString))
		{
			url = url.concat("?").concat(queryString);
		}

		HttpURLConnection httpURLConnection = (HttpURLConnection) (Objects.nonNull(proxy) ? new URL(url).openConnection(proxy) : new URL(url).openConnection());
		httpURLConnection.setRequestMethod(method);
		httpURLConnection.setConnectTimeout(5000);

		if(Objects.nonNull(headersMap))
		{
			for(Map.Entry<String, String> headers : headersMap.entrySet())
			{
				httpURLConnection.setRequestProperty(headers.getKey(), headers.getValue());
			}
		}

		httpURLConnection.setDoInput(true);

		if(Objects.nonNull(inputStream) && inputStream.available() > 0 && !method.equals(HttpGet.METHOD_NAME))
		{
			httpURLConnection.setDoOutput(true);
			copyInputStreamToOutputStream(inputStream, httpURLConnection.getOutputStream());
		}

		Map<String, String> responseHeadersMap = new TreeMap<>();
		Set<String> keySet = httpURLConnection.getHeaderFields().keySet();
		for(String headerName : keySet)
		{
			if(Objects.nonNull(headerName))
			{
				responseHeadersMap.put(headerName, httpURLConnection.getHeaderField(headerName));
			}
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

	public static boolean isValidURL(String url)
	{
		try
		{
			URL urlObject = new URL(url);
			String host = urlObject.getHost();
			int port = urlObject.getPort() != -1 ? urlObject.getPort() : urlObject.getDefaultPort();
			try(Socket socket = new Socket())
			{
				SocketAddress socketAddress = new InetSocketAddress(InetAddress.getByName(host), port);
				socket.connect(socketAddress);
			}

			return true;
		}
		catch(Exception e)
		{
			return false;
		}
	}
}
