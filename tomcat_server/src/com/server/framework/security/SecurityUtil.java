package com.server.framework.security;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletRegistration;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload2.core.FileItem;
import org.apache.commons.fileupload2.core.DiskFileItemFactory;
import org.apache.commons.fileupload2.jakarta.servlet6.JakartaServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.tomcat.websocket.server.WsServerContainer;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.server.framework.common.Configuration;
import com.server.framework.common.DateUtil;
import com.server.framework.common.Util;
import com.server.framework.job.JobUtil;
import com.server.framework.http.FormData;
import com.server.framework.http.HttpAPI;
import com.server.framework.user.User;

public class SecurityUtil
{
	public static final List<String> SKIP_AUTHENTICATION_ENDPOINTS = Arrays.asList("/_app/health", "/api/v1/(admin/)?authenticate", "/?(manager|tomcat)?login", "(/(resources|css|js)/.*)", "/api/v1/jobs", "/api/v1/admin/live/logs");
	public static final Function<String, Boolean> IS_REST_API = requestURI -> requestURI.matches("/api/(.*)");
	public static final Function<String, Boolean> IS_SKIP_AUTHENTICATION_ENDPOINTS = requestURI -> requestURI.matches(String.join("|", SKIP_AUTHENTICATION_ENDPOINTS));

	public static final Map<String,List<String>> VISITOR_META = new ConcurrentHashMap<>();

	private static final Logger LOGGER = Logger.getLogger(SecurityUtil.class.getName());

	public static boolean isResourceUri(ServletContext servletContext, String endPoint) throws MalformedURLException
	{
		return endPoint.contains(".") && Objects.nonNull(servletContext.getResource(endPoint)) && !endPoint.endsWith(".jsp") && !endPoint.endsWith(".html");
	}

	public static String getAuthToken()
	{
		String token = getCurrentRequest().getParameter("token");
		if(StringUtils.isNotBlank(token))
		{
			return token;
		}

		String authorizationHeader = ObjectUtils.defaultIfNull(getCurrentRequest().getHeader("Authorization"), StringUtils.EMPTY);
		Pattern pattern = Pattern.compile("Bearer (\\w+)");
		Matcher matcher = pattern.matcher(authorizationHeader);

		token = StringUtils.isNotEmpty(authorizationHeader) && matcher.matches() ? matcher.group(1) : StringUtils.EMPTY;

		if(StringUtils.isNotEmpty(token))
		{
			return token;
		}

		return StringUtils.EMPTY;
	}

	public static String getSessionId()
	{
		Cookie[] cookies = getCurrentRequest().getCookies();
		String tokenName = "iam_token";

		if(Objects.nonNull(cookies))
		{
			return Arrays.stream(cookies).filter(cookie -> cookie.getName().equals(tokenName)).map(Cookie::getValue).findFirst().orElse(StringUtils.EMPTY);
		}

		return StringUtils.EMPTY;
	}

	public static boolean isAdminCall(String requestURI)
	{
		return requestURI.matches("^(/api/v1)?/admin(.*)") || StringUtils.equals(SecurityFilter.SERVLET_CONTEXT_TL.get().getServletContextName(), "Manager");
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
		return url + request.getContextPath() + "/login?post=true";
	}

	public static String getOriginatingUserIP()
	{
		return StringUtils.defaultIfEmpty(getHeader(SecurityUtil.getCurrentRequest().getHeaders("x-forwarded-for")), SecurityUtil.getCurrentRequest().getRemoteAddr());
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
		writerErrorResponse(response, HttpStatus.SC_BAD_REQUEST, "error", message);
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

		DiskFileItemFactory diskFileItemFactory = DiskFileItemFactory.builder().get();
		try
		{
			File tempFile = new File(System.getProperty("java.io.tmpdir"));
			if(!tempFile.exists())
			{
				tempFile.mkdirs();
			}
			items = new JakartaServletFileUpload(diskFileItemFactory).parseRequest(request);
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

		for(String apiEndpoint : apiEndPoints)
		{
			if(apiEndpoint.endsWith("/*"))
			{
				if(endPoint.startsWith(apiEndpoint.replaceAll("/\\*", StringUtils.EMPTY)))
				{
					return true;
				}
			}
		}

		return apiEndPoints.contains(endPoint) || endPoint.startsWith("/manager")  ||  endPoint.startsWith("/tomcat") || isValidWebSocketEndPoint(endPoint) || Objects.nonNull(SecurityFilter.SERVLET_CONTEXT_TL.get().getResource(URLDecoder.decode(endPoint)));
	}

	public static boolean isValidWebSocketEndPoint(String endPoint)
	{
		Object mappingResult = ((WsServerContainer) SecurityFilter.SERVLET_CONTEXT_TL.get().getAttribute("jakarta.websocket.server.ServerContainer")).findMapping(endPoint);
		return Objects.nonNull(mappingResult);
	}

	public static String readFileAsString(String fileName) throws IOException
	{
		InputStream inputStream = SecurityFilter.SERVLET_CONTEXT_TL.get().getResourceAsStream("/WEB-INF/conf/".concat(fileName));

		StringWriter stringWriter = new StringWriter();

		IOUtils.copy(inputStream, stringWriter);

		return stringWriter.toString();
	}

	public static User getCurrentUser()
	{
		return SecurityFilter.CURRENT_USER_TL.get();
	}

	public static boolean isValidJSON(String data)
	{
		try
		{
			return StringUtils.isNotEmpty(new ObjectMapper().writeValueAsString(new ObjectMapper().readTree(data)));
		}
		catch(Exception e)
		{
			return false;
		}
	}

	public static HttpServletRequest getCurrentRequest()
	{
		return SecurityFilter.CURRENT_REQUEST_TL.get();
	}

	static void sendVisitorNotification() throws UnknownHostException
	{
		if(!isLoggedIn())
		{
			return;
		}
		String key = DateUtil.getFormattedCurrentTime("dd/MM/yyyy");
		List<String> visitorList = VISITOR_META.getOrDefault(key, new ArrayList<>());
		String remoteIp = getCurrentRequest().getRemoteAddr();
		String ip = InetAddress.getByName(remoteIp).isLoopbackAddress() ? getOriginatingUserIP() : remoteIp;
		if(visitorList.isEmpty())
		{
			VISITOR_META.clear();
			VISITOR_META.put(key, visitorList);
		}

		synchronized(VISITOR_META)
		{
			if(visitorList.contains(ip))
			{
				return;
			}
			visitorList.add(ip);
			String message = "<b>Public IP : </b> &nbsp;&nbsp;" + ip +
				"<br><br><b>Originating IP : </b> &nbsp;&nbsp;" + getOriginatingUserIP() +
				"<br><br><b>Request URL : </b> &nbsp;&nbsp;" + getCurrentRequest().getRequestURL().toString();

			JobUtil.scheduleJob(() -> Util.sendEmail("Visitor Alert - " + key, Configuration.getProperty("mail.user"), getMessageForVisitorNotification(ip, message)), 2);
		}
	}

	static String getMessageForVisitorNotification(String ip, String message) throws IOException
	{
		InetAddress inetAddress = InetAddress.getByName(ip);
		if(inetAddress.isSiteLocalAddress() || inetAddress.isLoopbackAddress())
		{
			return message;
		}
		Map<String, String> headersMap = new HashMap<>();
		headersMap.put("User-Agent", "Java - " + new Random().nextInt(900) + 100);

		if(StringUtils.isNotEmpty(Configuration.getProperty("ip2location.apikey")))
		{
			String ipLookUpUrl = "https://api.ip2location.io?key=" + Configuration.getProperty("ip2location.apikey") + "&ip=" + ip;

			JSONObject ipLookUpResponse = new JSONObject(HttpAPI.makeNetworkCall(ipLookUpUrl, HttpGet.METHOD_NAME, headersMap).getStringResponse());

			if(!ipLookUpResponse.has("error"))
			{
				message += "<br><br><br><u><b>ip2location.io IP lookup data : </b></u>";

				message += "<br><br><b>Country : </b>&nbsp;&nbsp;" + ipLookUpResponse.get("country_name");
				message += "<br><br><b>Region : </b>&nbsp;&nbsp;" + ipLookUpResponse.get("region_name");
				message += "<br><br><b>City : </b>&nbsp;&nbsp;" + ipLookUpResponse.get("city_name");
				message += "<br><br><b>Lat,Long : </b>&nbsp;&nbsp;" + ipLookUpResponse.get("latitude") + "," + ipLookUpResponse.get("longitude");
				message += "<br><br><b>ISP : </b>&nbsp;&nbsp;" + ipLookUpResponse.get("as");
			}
			else
			{
				LOGGER.info("IP look up failed with message " + ipLookUpResponse.getJSONObject("error").getString("error_message"));
			}
		}

		String ipLookUpStringResponse = HttpAPI.makeNetworkCall("https://ipapi.co/${IP}/json".replace("${IP}", ip), HttpGet.METHOD_NAME, headersMap).getStringResponse();
		JSONObject ipLookUpResponse = isValidJSON(ipLookUpStringResponse) ? new JSONObject(ipLookUpStringResponse) : new JSONObject();
		if(!ipLookUpResponse.has("error") && ipLookUpResponse.has("country_capital"))
		{
			message += "<br><br><br><u><b>ipapi.co IP lookup data : </b></u>";

			message += "<br><br><b>Country Capital, Currency : </b>&nbsp;&nbsp;" + ipLookUpResponse.get("country_capital") + ", " + ipLookUpResponse.get("currency");
			message += "<br><br><b>Country : </b>&nbsp;&nbsp;" + ipLookUpResponse.get("country_name");
			message += "<br><br><b>Region : </b>&nbsp;&nbsp;" + ipLookUpResponse.get("region");
			message += "<br><br><b>City : </b>&nbsp;&nbsp;" + ipLookUpResponse.get("city");
			message += "<br><br><b>Lat,Long : </b>&nbsp;&nbsp;" + ipLookUpResponse.get("latitude") + "," + ipLookUpResponse.get("longitude");
			message += "<br><br><b>ISP : </b>&nbsp;&nbsp;" + ipLookUpResponse.get("org");
		}
		else
		{
			LOGGER.info("IP look up failed with message " + ipLookUpResponse.optString("reason", ipLookUpStringResponse));
		}
		return message;
	}
}
