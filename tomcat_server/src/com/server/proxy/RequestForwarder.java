package com.server.proxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import com.server.common.Util;
import com.server.http.HttpAPI;
import com.server.http.HttpResponse;

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

		Enumeration<String> requestHeaderEnumeration = request.getHeaderNames();

		Map<String, String> headersMap = new HashMap<>();
		while(requestHeaderEnumeration.hasMoreElements())
		{
			String headerName = requestHeaderEnumeration.nextElement();
			if(headerName.equals("TARGET_URL") || headerName.equals("HTTP_METHOD"))
			{
				continue;
			}
			headersMap.put(headerName, request.getHeader(headerName));
		}

		HttpResponse httpResponse = HttpAPI.makeNetworkCall(url, method, request.getQueryString(), headersMap, request.getInputStream());

		for(String headerName : httpResponse.getResponseHeaders().keySet())
		{
			response.setHeader(headerName, httpResponse.getResponseHeaders().get(headerName));
		}

		response.setContentType(httpResponse.getContentType());
		response.setStatus(httpResponse.getStatus());

		HttpAPI.copyInputStreamToOutputStream(httpResponse.getInputStream(), response.getOutputStream());

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
