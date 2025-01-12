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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
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

	public static String getEncodedQueryString(Map<String, ?> dataMap)
	{
		List<NameValuePair> nameValuePairList = new ArrayList<>();
		for(String key : dataMap.keySet())
		{
			nameValuePairList.add(new BasicNameValuePair(key, ObjectUtils.defaultIfNull(dataMap.get(key), StringUtils.EMPTY).toString()));
		}

		return URLEncodedUtils.format(nameValuePairList, StandardCharsets.UTF_8);
	}

	public static InputStream getInputStreamForUrlFormEncoded(Map<String, ?> formMap)
	{
		return new ByteArrayInputStream(getEncodedQueryString(formMap).getBytes());
	}

	public static HttpResponse makeNetworkCall(String url, String method) throws IOException
	{
		return makeNetworkCall(url, method, (String) null, null, null, null);
	}

	public static HttpResponse makeNetworkCall(String url, String method, Map<String, String> headersMap) throws IOException
	{
		return makeNetworkCall(url, method, headersMap, null, null);
	}

	public static HttpResponse makeNetworkCall(String url, String method, Map<String, String> headersMap, JSONObject jsonObject) throws IOException
	{
		return makeNetworkCall(url, method, headersMap, null, jsonObject);
	}

	public static HttpResponse makeNetworkCall(String url, String method, Map<String, String> headersMap, Map<String, String> parametersMap) throws IOException
	{
		return makeNetworkCall(url, method, headersMap, parametersMap, null);
	}

	public static HttpResponse makeNetworkCall(String url, String method, Map<String, String> headersMap, Map<String, String> parametersMap, JSONObject jsonBody) throws IOException
	{
		return makeNetworkCall(url, method, headersMap, parametersMap, jsonBody, null);
	}

	public static HttpResponse makeNetworkCall(String url, String method, Map<String, String> headersMap, Map<String, String> parametersMap, JSONObject jsonBody, Proxy proxy) throws IOException
	{
		Map<String, String> headersMapWrapper = new HashMap<>();
		if(method.equals(HttpPost.METHOD_NAME) && Objects.nonNull(jsonBody))
		{
			headersMapWrapper.put("Content-Type", "application/json");
		}
		headersMapWrapper.putAll(ObjectUtils.defaultIfNull(headersMap, new HashMap<>()));
		parametersMap = ObjectUtils.defaultIfNull(parametersMap, new HashMap<>());
		List<String> queryStringList = parametersMap.entrySet().stream().map(entrySet -> entrySet.getKey().concat("=").concat(URLEncoder.encode(entrySet.getValue(), StandardCharsets.UTF_8))).collect(Collectors.toList());

		return makeNetworkCall(url, method, String.join("&", queryStringList), headersMapWrapper, Objects.nonNull(jsonBody) ? new ByteArrayInputStream(jsonBody.toString().getBytes()) : null, proxy);
	}

	public static HttpResponse makeNetworkCall(String url, String method, String queryString, Map<String, String> headersMap, InputStream inputStream, Proxy proxy) throws IOException
	{
		if(StringUtils.isNotEmpty(queryString))
		{
			url = !StringUtils.contains(url, "?") ? url.concat("?").concat(queryString) : StringUtils.contains(url, "&") ? url.concat(queryString) : url.concat("&").concat(queryString);
		}

		HttpURLConnection httpURLConnection = (HttpURLConnection) (Objects.nonNull(proxy) ? new URL(url).openConnection(proxy) : new URL(url).openConnection());
		httpURLConnection.setRequestMethod(method);
		httpURLConnection.setConnectTimeout(5000);

		if(Objects.nonNull(headersMap))
		{
			for(Map.Entry<String, String> headers : headersMap.entrySet())
			{
				httpURLConnection.setRequestProperty(headers.getKey(), StringUtils.isNotEmpty(headers.getValue()) ? headers.getValue().replaceAll("\n", StringUtils.EMPTY).trim() : null);
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
