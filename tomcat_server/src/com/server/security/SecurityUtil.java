package com.server.security;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.tomcat.websocket.server.WsServerContainer;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.server.security.http.FormData;
import com.server.security.user.User;

public class SecurityUtil
{
	public static final List<String> SKIP_AUTHENTICATION_ENDPOINTS = Arrays.asList("/_app/health", "/api/v1/(admin/)?authenticate", "/(admin/)?login", "(/(resources|css|js)/.*)", "/api/v1/jobs", "/api/v1/run");
	public static final Function<String, Boolean> IS_REST_API = requestURI -> requestURI.matches("/api/(.*)");
	public static final Function<String, Boolean> IS_SKIP_AUTHENTICATION_ENDPOINTS = requestURI -> requestURI.matches(String.join("|", SKIP_AUTHENTICATION_ENDPOINTS));

	public static boolean isResourceUri(ServletContext servletContext, String endPoint) throws MalformedURLException
	{
		return Objects.nonNull(servletContext.getResource(endPoint)) && !endPoint.endsWith(".jsp") && !endPoint.endsWith(".html");
	}

	public static String getSessionId(HttpServletRequest request)
	{
		String requestURI = request.getRequestURI();
		Cookie[] cookies = request.getCookies();
		String authorizationHeader = ObjectUtils.defaultIfNull(request.getHeader("Authorization"), StringUtils.EMPTY);
		Pattern pattern = Pattern.compile("Bearer (\\d+)");
		Matcher matcher = pattern.matcher(authorizationHeader);

		String token = StringUtils.isNotEmpty(authorizationHeader) && matcher.matches() ? matcher.group(1) : StringUtils.EMPTY;

		if(StringUtils.isNotEmpty(token))
		{
			return token;
		}

		String tokenName = "iam_token";

		if(Objects.nonNull(cookies))
		{
			return Arrays.stream(cookies).filter(cookie -> cookie.getName().equals(tokenName)).map(Cookie::getValue).findFirst().orElse(StringUtils.EMPTY);
		}

		return StringUtils.EMPTY;
	}

	public static boolean isAdminCall(String requestURI)
	{
		return requestURI.matches("^(/api/v1)?/admin(.*)|/manager(.*)");
	}

	public static boolean isLoggedIn()
	{
		return Objects.nonNull(getCurrentUser());
	}

	public static boolean canSkipAuthentication(String requestURI)
	{
		return IS_SKIP_AUTHENTICATION_ENDPOINTS.apply(requestURI);
	}

	public static String getRedirectURI(HttpServletRequest request) throws MalformedURLException
	{
		URL urlObject = new URL(request.getRequestURL().toString());
		int port = urlObject.getPort() != -1 ? urlObject.getPort() : urlObject.getDefaultPort();
		String url = urlObject.getProtocol() + "://" + urlObject.getHost() + ":" + port;
		return url + "/login?post=true";
	}

	public static String getUserIP(HttpServletRequest request)
	{
		return StringUtils.defaultIfEmpty(getHeader(request.getHeaders("x-forwarded-for")), request.getRemoteAddr());
	}

	public static String getHeader(Enumeration<String> headersEnumeration)
	{
		String headerValue = StringUtils.EMPTY;
		while(headersEnumeration.hasMoreElements())
		{
			headerValue += headersEnumeration.nextElement() + ",";
		}
		return headerValue.replaceAll(",$", StringUtils.EMPTY);
	}

	public static void writerErrorResponse(HttpServletResponse response, String message) throws IOException
	{
		writerErrorResponse(response, HttpStatus.SC_BAD_REQUEST, null, message);
	}

	public static void writerErrorResponse(HttpServletResponse response, int statusCode, String code, String message) throws IOException
	{
		response.setStatus(statusCode);

		Map<String, String> responseMap = new HashMap<>();
		responseMap.put("error", message);
		responseMap.put("code", code);

		writeJSONResponse(response, responseMap);
	}

	public static void writerErrorResponse(HttpServletResponse response, int statusCode, String code, String message, Map<String, String> additionalData) throws IOException
	{
		response.setStatus(statusCode);

		Map<String, String> responseMap = new HashMap<>();
		responseMap.put("error", message);
		responseMap.put("code", code);
		responseMap.putAll(additionalData);

		writeJSONResponse(response, responseMap);
	}

	public static Map<String, FormData> parseMultiPartFormData(HttpServletRequest request) throws IOException
	{
		List<FileItem> items;

		DiskFileItemFactory diskFileItemFactory = new DiskFileItemFactory();
		try
		{
			items = new ServletFileUpload(diskFileItemFactory).parseRequest(request);
		}
		catch(Exception e)
		{
			return new HashMap<>();
		}

		Map<String, FormData> formDataMap = new HashMap<>();
		for(FileItem item : items)
		{
			if(item.isFormField())
			{
				String fieldName = item.getFieldName();
				String fieldValue = item.getString();

				FormData formData = new FormData();
				formData.setValue(fieldValue);
				formDataMap.put(fieldName, formData);
			}
			else
			{
				String fieldName = item.getFieldName();

				FormData formData = formDataMap.getOrDefault(fieldName, new FormData());
				formData.setIsFile(true);

				List<FormData.FileData> fileDataList = formData.getFileDataList();

				FormData.FileData fileData = new FormData.FileData(item.getName(), readAllBytes(item.getInputStream()));
				fileDataList.add(fileData);

				formDataMap.put(item.getFieldName(), formData);
			}
		}

		return formDataMap;
	}

	public static void writeJSONResponse(HttpServletResponse response, Object responseObject) throws IOException
	{
		response.setContentType("application/json");

		ObjectMapper objectMapper = new ObjectMapper();
		response.getWriter().print(objectMapper.writeValueAsString(responseObject));
	}

	public static void writeSuccessJSONResponse(HttpServletResponse response, String responseMessage) throws IOException
	{
		Map<String, String> responseMap = new HashMap<>();
		responseMap.put("message", responseMessage);
		writeJSONResponse(response, responseMap);
	}

	public static byte[] readAllBytes(InputStream inputStream) throws IOException
	{

		byte[] bytes = new byte[inputStream.available()];
		inputStream.read(bytes);
		return bytes;
	}

	public static JSONObject getJSONObject(HttpServletRequest request) throws IOException
	{
		return new JSONObject(request.getReader().lines().collect(Collectors.joining()));
	}

	public static boolean isValidEndPoint(String endPoint) throws MalformedURLException
	{
		List<String> apiEndPoints = SecurityFilter.SERVLET_CONTEXT_TL.get().getServletRegistrations().keySet().stream()
			.map(SecurityFilter.SERVLET_CONTEXT_TL.get()::getServletRegistration)
			.map(ServletRegistration::getMappings)
			.flatMap(Collection::stream)
			.collect(Collectors.toList());

		return apiEndPoints.contains(endPoint) || endPoint.startsWith("/manager")  ||  endPoint.startsWith("/tomcat") || isValidWebSocketEndPoint(endPoint) || Objects.nonNull(SecurityFilter.SERVLET_CONTEXT_TL.get().getResource(URLDecoder.decode(endPoint)));
	}

	public static boolean isValidWebSocketEndPoint(String endPoint)
	{
		Object mappingResult = ((WsServerContainer) SecurityFilter.SERVLET_CONTEXT_TL.get().getAttribute("javax.websocket.server.ServerContainer")).findMapping(endPoint);
		return Objects.nonNull(mappingResult);
	}

	public static String readFileAsString(String fileName) throws IOException
	{
		InputStream inputStream = SecurityFilter.SERVLET_CONTEXT_TL.get().getResourceAsStream("/WEB-INF/conf/".concat(fileName));

		StringWriter stringWriter = new StringWriter();

		IOUtils.copy(inputStream, stringWriter);

		return stringWriter.toString();
	}

	public static String getFormattedCurrentTime()
	{
		return getFormattedTime(System.currentTimeMillis());
	}

	public static String getFormattedTime(Long timeInMilliseconds)
	{
		return LocalDateTime.ofInstant(Instant.ofEpochMilli(timeInMilliseconds), ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("hh : mm a"));
	}

	public static User getCurrentUser()
	{
		return SecurityFilter.CURRENT_USER_TL.get();
	}
}
