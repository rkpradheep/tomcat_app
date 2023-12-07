package com.server.proxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.server.common.Util;

public class RequestForwarder extends HttpServlet
{
	private static final String METHOD_DELETE = "DELETE";
	private static final String METHOD_HEAD = "HEAD";
	private static final String METHOD_GET = "GET";
	private static final String METHOD_OPTIONS = "OPTIONS";
	private static final String METHOD_POST = "POST";
	private static final String METHOD_PUT = "PUT";
	private static final String METHOD_TRACE = "TRACE";
	private static final List<String> VALID_HTTP_METHOD;

	private static final Logger LOGGER = Logger.getLogger(RequestForwarder.class.getName());

	static
	{
		VALID_HTTP_METHOD = Collections.unmodifiableList(Arrays.asList(METHOD_DELETE, METHOD_HEAD, METHOD_GET, METHOD_OPTIONS, METHOD_POST, METHOD_PUT, METHOD_TRACE));
	}

	@Override
	public void service(HttpServletRequest request, HttpServletResponse response) throws IOException
	{
		String url = request.getHeader("TARGET-URL");
		String method = request.getHeader("HTTP-METHOD");

		LOGGER.log(Level.INFO, "Request forwarding received for URL {0} and Method {1}", new Object[] {url, method});

		if(url == null || method == null || !isValidHttpMethod(method) || !isValidURL(url))
		{
			Util.writerErrorResponse(response, "Invalid value passed for TARGET-URL or HTTP-METHOD");
			return;
		}
		//		String jsonData = request.getReader().lines().collect(Collectors.joining(""));
		//
		//
		//		try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
		//			String targetServerUrl = "https://api.uatyespayhub.in/services/limit/da";
		//
		//			HttpPost httpPost = new HttpPost(targetServerUrl);
		//
		//			httpPost.setHeader("token", request.getHeader("token"));
		//			httpPost.setHeader("key", request.getHeader("key"));
		//			httpPost.setHeader("iv", request.getHeader("iv"));
		//			httpPost.setHeader("partner", request.getHeader("partner"));
		//			httpPost.setHeader("Content-Type", "application/json");
		//
		//			httpPost.setEntity(new StringEntity(jsonData));
		//
		//			HttpResponse targetServerResponse = httpClient.execute(httpPost);
		//
		//			String responseBody = EntityUtils.toString(targetServerResponse.getEntity());
		//
		//			if(targetServerResponse.getHeaders("key").length > 0)
		//			{
		//				response.setHeader("key", targetServerResponse.getHeaders("key")[0].getValue());
		//				response.setHeader("iv", targetServerResponse.getHeaders("iv")[0].getValue());
		//				response.setHeader("hash", targetServerResponse.getHeaders("hash")[0].getValue());
		//			}
		//
		//			response.getWriter().write(responseBody);
		//		} catch (Exception e) {
		//		}

		if(request.getParameterNames().hasMoreElements())
		{
			url = url.concat("?").concat(request.getQueryString());
		}

		HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(url).openConnection();
		httpURLConnection.setRequestMethod(method);
		httpURLConnection.setConnectTimeout(5000);

		Enumeration<String> requestHeaderEnumeration = request.getHeaderNames();
		while(requestHeaderEnumeration.hasMoreElements())
		{
			String headerName = requestHeaderEnumeration.nextElement();
			if(headerName.equals("TARGET_URL") || headerName.equals("HTTP_METHOD"))
			{
				continue;
			}
			httpURLConnection.setRequestProperty(headerName, request.getHeader(headerName));
		}

		httpURLConnection.setDoInput(true);

		if(request.getInputStream().available() > 0 && !method.equals(METHOD_GET))
		{
			httpURLConnection.setDoOutput(true);
			copyInputStreamToOutputStream(request.getInputStream(), httpURLConnection.getOutputStream());
		}

		Set<String> keySet = httpURLConnection.getHeaderFields().keySet();
		for(String headerName : keySet)
		{
			response.setHeader(headerName, httpURLConnection.getHeaderField(headerName));
		}

		response.setContentType(httpURLConnection.getContentType());
		response.setStatus(httpURLConnection.getResponseCode());

		copyInputStreamToOutputStream(getStream(httpURLConnection), response.getOutputStream());

	}

	void copyInputStreamToOutputStream(InputStream inputStream, OutputStream outputStream) throws IOException
	{
		byte[] bytes = new byte[1024];
		int len = -1;

		while((len = inputStream.read(bytes)) != -1)
		{
			outputStream.write(bytes, 0, len);
		}
		outputStream.flush();
	}

	InputStream getStream(HttpURLConnection httpURLConnection)
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

	boolean isValidHttpMethod(String method)
	{
		return VALID_HTTP_METHOD.contains(method);
	}

	boolean isValidURL(String url)
	{
		try
		{
			new URL(url);
			return true;
		}
		catch(MalformedURLException e)
		{
			return false;
		}
	}
}
