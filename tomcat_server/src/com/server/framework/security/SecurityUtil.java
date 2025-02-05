package com.server.framework.security;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletRegistration;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import sun.net.www.http.PosterOutputStream;

import javax.net.ssl.HttpsURLConnection;

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
import com.server.framework.common.EntityType;
import com.server.framework.common.Util;
import com.server.framework.http.HttpContext;
import com.server.framework.job.JobUtil;
import com.server.framework.http.FormData;
import com.server.framework.http.HttpAPI;
import com.server.framework.persistence.Column;
import com.server.framework.persistence.Criteria;
import com.server.framework.persistence.DataAccess;
import com.server.framework.persistence.Row;
import com.server.framework.persistence.UpdateQuery;
import com.server.framework.user.User;
import com.server.table.constants.HTTPLOG;

public class SecurityUtil
{
	public static final List<String> SKIP_AUTHENTICATION_ENDPOINTS = Arrays.asList("/_app/health", "/api/v1/(admin/)?authenticate", "/?(manager|tomcat)?login", "(/((resources|css|js)/.*)|favicon.ico)", "/api/v1/jobs", "/payoutlogs", "/api/v1/payout/httplogs", "/api/v1/admin/live/logs", "/.well-known/.*", "(/dbtool.jsp|/sasstats|/api/v1/(sas|zoho)/.*)");
	public static final Function<String, Boolean> IS_REST_API = requestURI -> requestURI.matches("/api/(.*)");
	public static final Function<String, Boolean> IS_SKIP_AUTHENTICATION_ENDPOINTS = requestURI -> requestURI.matches(String.join("|", SKIP_AUTHENTICATION_ENDPOINTS));

	public static final Map<String, List<String>> VISITOR_META = new ConcurrentHashMap<>();

	private static final Logger LOGGER = Logger.getLogger(SecurityUtil.class.getName());

	public static boolean isResourceUri(ServletContext servletContext, String endPoint) throws MalformedURLException
	{
		return endPoint.matches("(/(((resources|css|js)/.*)|favicon.ico))");
	}

	public static String getUploadsPath()
	{
		return Util.HOME_PATH + "/tomcat_build/webapps/ROOT/uploads";
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
		return getCookieValue("iam_token");
	}

	public static String getCookieValue(String cookieName)
	{
		Cookie[] cookies = getCurrentRequest().getCookies();
		if(Objects.nonNull(cookies))
		{
			return Arrays.stream(cookies).filter(cookie -> cookie.getName().equals(cookieName)).map(Cookie::getValue).findFirst().orElse(StringUtils.EMPTY);
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
				formData.setContentType(item.getContentType());
				formDataMap.put(fieldName, formData);
			}
			else
			{
				String fieldName = item.getFieldName();

				FormData formData = formDataMap.getOrDefault(fieldName, new FormData());
				formData.setIsFile(true);

				List<FormData.FileData> fileDataList = formData.getFileDataList();

				FormData.FileData fileData = new FormData.FileData(item.getName(), readAllBytes(item.getInputStream()), item.getContentType());
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

	public static void writeHTMLResponse(HttpServletResponse response, String html) throws IOException
	{
		response.setContentType("text/html; charset=UTF-8");

		response.getWriter().print(html);
	}

	public static void writeSuccessJSONResponse(HttpServletResponse response, String responseMessage) throws IOException
	{
		writeSuccessJSONResponse(response, responseMessage, null);
	}

	public static void writeSuccessJSONResponse(HttpServletResponse response, String responseMessage, Object data) throws IOException
	{
		Map<String, Object> responseMap = new HashMap<>();
		responseMap.put("message", responseMessage);
		responseMap.put("data", data);
		writeJSONResponse(response, responseMap);
	}

	public static byte[] readAllBytes(InputStream inputStream) throws IOException
	{

		byte[] bytes = new byte[inputStream.available()];
		inputStream.read(bytes);
		return bytes;
	}

	public static JSONObject getCurrentRequestJSONObject() throws IOException
	{
		return getJSONObject(getCurrentRequest());
	}

	public static JSONObject getJSONObject(HttpServletRequest request) throws IOException
	{
		if(!StringUtils.equals(request.getContentType(), "application/json"))
		{
			return null;
		}
		if(Objects.nonNull(request.getAttribute("JSON_PAYLOAD")))
		{
			return new JSONObject(((JSONObject) request.getAttribute("JSON_PAYLOAD")).toMap());
		}
		try
		{
			JSONObject jsonObject = new JSONObject(request.getReader().lines().collect(Collectors.joining()));
			request.setAttribute("JSON_PAYLOAD", jsonObject);
			return new JSONObject(jsonObject.toMap());
		}
		catch(Exception e)
		{
			return null;
		}
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

		return apiEndPoints.contains(endPoint) || endPoint.startsWith("/manager") || endPoint.startsWith("/tomcat") || isValidWebSocketEndPoint(endPoint) || Objects.nonNull(SecurityFilter.SERVLET_CONTEXT_TL.get().getResource(URLDecoder.decode(endPoint)));
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

	public static String getCurrentRequestDomain()
	{
		try
		{
			URL url = new URL(getCurrentRequest().getRequestURL().toString());
			return url.getProtocol() + "://" + url.getHost();
		}
		catch(Exception e)
		{
			return getCurrentRequest().getRequestURL().toString();
		}
	}

	public static void addHTTPLog() throws IOException
	{
		addHTTPLog(EntityType.COMMON);
	}

	public static void addHTTPLog(EntityType entityType) throws IOException
	{
		if(!Configuration.getBoolean("need.http.logs"))
		{
			return;
		}
		HttpServletRequest request = getCurrentRequest();
		JSONObject payload = SecurityUtil.getCurrentRequestJSONObject();

		Map<String,String> requestHeaders = new HashMap<>();
		SecurityUtil.getCurrentRequest().getHeaderNames().asIterator().forEachRemaining(headerName-> requestHeaders.put(headerName, request.getHeader(headerName)));
		HttpLogRequest.Builder builder = new HttpLogRequest.Builder()
			.setUrl(request.getRequestURL().toString())
			.setMethod(request.getMethod())
			.setIP(request.getRemoteAddr())
			.setRequestHeaders(requestHeaders.isEmpty() ? null : new JSONObject(requestHeaders).toString())
			.setJsonPayLoad(Objects.nonNull(payload) ? payload.toString() : null)
			.setQueryString(request.getQueryString());

		JSONObject jsonObject = getJSONObject(request);

		builder.setJsonPayLoad(Objects.isNull(jsonObject) ? null : jsonObject.toString())
			.setThreadName(Thread.currentThread().getName())
			.setEntityType(entityType);

		addHTTPLog(builder.build());
	}

	public static Long addHttpLog(HttpURLConnection connection) throws IOException
	{
 		PosterOutputStream outputStream = StringUtils.equals(connection.getRequestProperty("Content-Type"), "application/json") ? (PosterOutputStream) connection.getOutputStream() : null;
		String requestJSON = Objects.nonNull(outputStream) ? new String(outputStream.toByteArray()) : null;

		Map<String,String> requestHeaders = connection.getRequestProperties().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entrySet-> String.join("", entrySet.getValue())));
		String requestHeadersString = requestHeaders.isEmpty() ? null : new JSONObject(requestHeaders).toString();

		HttpLogRequest.Builder builder = new HttpLogRequest.Builder()
			.setUrl(getURLString(connection.getURL()))
			.setMethod(connection.getRequestMethod())
			.setIP(Objects.isNull(getCurrentRequest()) ? null : getCurrentRequest().getRemoteAddr())
			.setQueryString(connection.getURL().getQuery())
			.setThreadName(Thread.currentThread().getName())
			.setOutgoing(true)
			.setJsonPayLoad(requestJSON)
			.setRequestHeaders(requestHeadersString)
			.setEntityType(EntityType.COMMON);

		return addHTTPLog(builder.build());
	}

	public static Long addHTTPLog(HttpLogRequest httpLogRequest)
	{
		try
		{
			String queryString = httpLogRequest.getQueryString();
			Map<String,String> parameterMap = StringUtils.isEmpty(queryString) ? null : Arrays.stream(queryString.split("&")).map(query-> query.split("=")).collect(Collectors.toMap(name-> name[0], value-> value[1]));
			String parameters = Objects.nonNull(parameterMap) ? new JSONObject(parameterMap).toString() : null;

			Row row = new Row(HTTPLOG.TABLE);
			row.set(HTTPLOG.URL, httpLogRequest.getUrl());
			row.set(HTTPLOG.METHOD, httpLogRequest.getMethod());
			row.set(HTTPLOG.IP, httpLogRequest.getIP());
			row.set(HTTPLOG.PARAMETERS, parameters);
			row.set(HTTPLOG.REQUESTDATA, httpLogRequest.getJsonPayLoad());
			row.set(HTTPLOG.THREADNAME, httpLogRequest.getThreadName());
			row.set(HTTPLOG.ENTITYTYPE, httpLogRequest.getEntityType().getValue());
			row.set(HTTPLOG.STATUSCODE, httpLogRequest.getStatusCode());
			row.set(HTTPLOG.ISOUTGOING, httpLogRequest.isOutgoing() ? 1 : 0);
			row.set(HTTPLOG.REQUESTHEADERS, httpLogRequest.getRequestHeaders());
			row.set(HTTPLOG.RESPONSEHEADERS, httpLogRequest.getResponseHeaders());

			DataAccess.add(row);

			return (long) row.get(HTTPLOG.ID);
		}
		catch(Exception e)
		{
			LOGGER.log(Level.SEVERE, "Exception occurred", e);
			return null;
		}
	}

	public static void updateHttpLog(long httpLogId, HttpURLConnection connection)
	{
		try
		{
			Map<String,String> responseHeaders = connection.getHeaderFields().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entrySet-> String.join("", entrySet.getValue())));
			String reponseHeadersString = responseHeaders.isEmpty() ? null : new JSONObject(responseHeaders).toString();

			UpdateQuery updateQuery = new UpdateQuery(HTTPLOG.TABLE);
			updateQuery.setCriteria(new Criteria(Column.getColumn(HTTPLOG.TABLE, HTTPLOG.ID), httpLogId, Criteria.Constants.EQUAL));

			updateQuery.setValue(HTTPLOG.STATUSCODE, connection.getResponseCode());
			InputStream inputStream = connection.getErrorStream();
			inputStream = ObjectUtils.defaultIfNull(inputStream, connection.getInputStream());
			updateQuery.setValue(HTTPLOG.RESPONSEDATA, new String(inputStream.readAllBytes()));
			updateQuery.setValue(HTTPLOG.RESPONSEHEADERS, reponseHeadersString);
			DataAccess.update(updateQuery);
		}
		catch(Exception e)
		{
			LOGGER.log(Level.SEVERE, "Exception occurred", e);
		}
	}

	public static String getURLString(URL url)
	{
		int port = url.getPort() == -1 ? url.getDefaultPort() : url.getPort();
		return url.getProtocol() + "://" + url.getHost().concat(":").concat(String.valueOf(port)).concat(url.getPath());
	}

	static void sendVisitorNotification() throws UnknownHostException
	{
		if(!Configuration.getBoolean("send.visitor.notification") || !isLoggedIn())
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

			JSONObject ipLookUpResponse = new JSONObject(HttpAPI.makeNetworkCall(new HttpContext(ipLookUpUrl, HttpGet.METHOD_NAME).setHeadersMap(headersMap)).getStringResponse());

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

		String ipLookUpStringResponse = HttpAPI.makeNetworkCall(new HttpContext("https://ipapi.co/${IP}/json".replace("${IP}", ip), HttpGet.METHOD_NAME).setHeadersMap(headersMap)).getStringResponse();
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
