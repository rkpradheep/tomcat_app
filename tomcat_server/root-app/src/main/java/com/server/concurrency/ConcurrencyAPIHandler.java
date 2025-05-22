package com.server.concurrency;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.server.framework.common.Util;
import com.server.framework.http.FormData;
import com.server.framework.http.HttpAPI;
import com.server.framework.http.HttpContext;
import com.server.framework.http.HttpResponse;
import com.server.framework.security.SecurityUtil;

public class ConcurrencyAPIHandler extends HttpServlet
{
	private static final Logger LOGGER = Logger.getLogger(ConcurrencyAPIHandler.class.getName());

	@Override
	public void service(HttpServletRequest request, HttpServletResponse response) throws IOException
	{
		Map<String, FormData> formDataMap = SecurityUtil.parseMultiPartFormData(request);

		JSONObject jsonObject = new JSONObject(formDataMap.get("meta_json").getValue());
		int concurrencyCalls = jsonObject.getInt("concurrency_calls");

		if(concurrencyCalls > 100 && !StringUtils.equals(jsonObject.getString("password"), "1155"))
		{
			SecurityUtil.writerErrorResponse(response, "Concurrent call value is above 100 and password provided is invalid.");
			return;
		}

		JSONObject params = jsonObject.getJSONObject("params");

		Pattern urlParamsPatters = Pattern.compile("(.*)\\?(.*)");
		Matcher urlParamsMatcher = urlParamsPatters.matcher(jsonObject.getString("url"));
		if(urlParamsMatcher.matches())
		{
			for(String queryParam : urlParamsMatcher.group(2).split("&"))
			{
				String[] queryParamSplit = queryParam.split("=");
				params.put(queryParamSplit[0].trim(), queryParamSplit[1].trim());
			}
			jsonObject.put("url", urlParamsMatcher.group(1));
		}

		String url = jsonObject.getString("url");
		String method = jsonObject.getString("method");
		String headersFromRequest = jsonObject.opt("headers").toString();

		JSONObject proxyDetails = jsonObject.optJSONObject("proxy_meta");
		Proxy proxy = getProxy(proxyDetails);

		JSONObject headers = Objects.nonNull(Util.getJSONFromString(headersFromRequest)) ? new JSONObject(headersFromRequest) : parseChromeHeaders(headersFromRequest);

		formDataMap.remove("meta_json");

		Map<String, String> headersMap = new TreeMap<>(headers.keySet().stream().collect(Collectors.toMap(key -> key, headers::getString)));
		headersMap.remove("Content-Type");
		headersMap.remove("Content-Length");
		headersMap.remove("Accept-Encoding");

		String previousForwardedFor = headersMap.getOrDefault("x-forwarded-for", StringUtils.EMPTY);
		previousForwardedFor = StringUtils.isNotEmpty(previousForwardedFor) ? previousForwardedFor.concat(",") : StringUtils.EMPTY;

		headersMap.put("x-forwarded-for", previousForwardedFor.concat(request.getRemoteAddr()));

		if(!HttpAPI.isValidURL(url))
		{
			SecurityUtil.writerErrorResponse(response, "API URL provided is invalid. Please check and try again.");
			return;
		}

		List<Map> responseList = new ArrayList<>();

		AtomicInteger atomicInteger = new AtomicInteger(0);

		Runnable runnable = () -> {
			try
			{
				Map<String, String> finalHeadersMap = new HashMap<>(headersMap);
				HttpResponse httpResponse = HttpAPI.makeNetworkCall(new HttpContext(url, method).setParametersMap(params.toMap()).setBody(getInputStream(formDataMap, finalHeadersMap)).setHeadersMap(finalHeadersMap).setProxy(proxy));
				StringWriter stringWriter = new StringWriter();
				IOUtils.copy(httpResponse.getInputStream(), stringWriter);
				JSONObject responseJSON = new JSONObject();
				Object responseObject = SecurityUtil.isValidJSON(stringWriter.toString()) ? new ObjectMapper().readTree(stringWriter.toString()) : stringWriter.toString(); //To preserve order
				synchronized(responseList)
				{
					responseJSON.put("Response " + atomicInteger.incrementAndGet(), responseObject);
					responseList.add(responseJSON.toMap());
				}
			}
			catch(Exception e)
			{
				LOGGER.log(Level.SEVERE, "Exception occurred ", e);
			}
		};

		List<Runnable> runnableList = new ArrayList<>();
		IntStream.range(0, concurrencyCalls).forEach(value -> runnableList.add(runnable));

		boolean oldDisableHttpsLog = SecurityUtil.getDisableHttpLog();
		try
		{
			oldDisableHttpsLog = SecurityUtil.setDisableHttpLog(true);
			executeAsynchronously(runnableList);
		}
		finally
		{
			SecurityUtil.setDisableHttpLog(oldDisableHttpsLog);
		}

		LOGGER.log(Level.INFO, "Response list size {0}", responseList.size());

		SecurityUtil.writeJSONResponse(response, responseList);
	}

	public static void executeAsynchronously(List<Runnable> runnableList)
	{
		List<Future<?>> futureList = new ArrayList<>();
		ExecutorService executorService = Executors.newFixedThreadPool(runnableList.size());

		for(Runnable runnable : runnableList)
		{
			futureList.add(executorService.submit(runnable));
		}
		LOGGER.log(Level.INFO, "Future list size {0}", futureList.size());
		for(Future<?> future : futureList)
		{
			try
			{
				future.get();
			}
			catch(Exception e)
			{
				LOGGER.log(Level.SEVERE, "Exception occurred ", e);
			}
		}

		executorService.shutdown();
	}

	static InputStream getInputStream(Map<String, FormData> formDataMap, Map<String, String> headersMap) throws IOException
	{
		String jsonObjectString = formDataMap.getOrDefault("json_payload", new FormData()).getValue();

		if(StringUtils.isNotEmpty(jsonObjectString))
		{
			headersMap.put("Content-Type", "application/json");
			return new ByteArrayInputStream(jsonObjectString.getBytes());
		}

		String formUrlEncodedJSONString = formDataMap.getOrDefault("form_urlencoded", new FormData()).getValue();
		if(StringUtils.isNotEmpty(formUrlEncodedJSONString))
		{
			headersMap.put("Content-Type", "application/x-www-form-urlencoded");
			return new ByteArrayInputStream(HttpAPI.getEncodedQueryString(new JSONObject(formUrlEncodedJSONString).toMap()).getBytes());
		}

		if(formDataMap.isEmpty())
		{
			return null;
		}

		MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
		for(Map.Entry<String, FormData> formDataEntry : formDataMap.entrySet())
		{
			FormData formData = formDataEntry.getValue();
			String name = formDataEntry.getKey();

			if(formData.isFile())
			{
				for(FormData.FileData fileData : formData.getFileDataList())
				{
					String mimeType = ObjectUtils.defaultIfNull(URLConnection.guessContentTypeFromName(fileData.getFileName()), ContentType.APPLICATION_OCTET_STREAM.getMimeType());
					multipartEntityBuilder.addBinaryBody(name, fileData.getBytes(), ContentType.parse(mimeType), fileData.getFileName());
				}
			}
			else
			{
				ContentType contentType = Objects.nonNull(Util.getJSONFromString(formData.getValue())) ? ContentType.APPLICATION_JSON : ContentType.TEXT_PLAIN;
				multipartEntityBuilder.addTextBody(name, formData.getValue(), contentType);
			}
		}

		HttpEntity httpEntity = multipartEntityBuilder.build();

		String boundary = ContentType.get(httpEntity).getParameter("boundary");
		headersMap.put("Content-type", "multipart/form-data; boundary=".concat(boundary));

		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		httpEntity.writeTo(byteArrayOutputStream);
		return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
	}

	static JSONObject parseChromeHeaders(String chromeHeaders)
	{
		BufferedReader bufferedReader = new BufferedReader(new StringReader(chromeHeaders));
		List<String> headersList = bufferedReader.lines().collect(Collectors.toList());
		JSONObject parsedHeader = new JSONObject();
		for(int i = 0; i < headersList.size(); i += 2)
		{
			String key = headersList.get(i).trim();
			if(key.startsWith(":"))
			{
				key = headersList.get(i).replaceFirst(":", "");
			}
			if(key.endsWith(":"))
			{
				key = key.replaceAll(":$", "");
			}

			parsedHeader.put(key, headersList.get(i + 1).trim());
		}
		return parsedHeader;
	}

	static Proxy getProxy(JSONObject proxyDetails)
	{
		if(!(Objects.nonNull(proxyDetails) && StringUtils.isNotEmpty(proxyDetails.optString("ip"))))
		{
			return null;
		}
		String proxyUserName = proxyDetails.optString("user_name");
		String proxyPassword = proxyDetails.optString("password");
		String ip = proxyDetails.getString("ip");
		int port = proxyDetails.optInt("port", 3128);

		if(StringUtils.isNotEmpty(proxyUserName) && StringUtils.isNotEmpty(proxyPassword))
		{
			Authenticator.setDefault(new Authenticator()
			{
				@Override
				protected PasswordAuthentication getPasswordAuthentication()
				{
					return new PasswordAuthentication(proxyUserName, proxyPassword.toCharArray());
				}
			});
		}
		else
		{
			Authenticator.setDefault(null);
		}
		return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(ip, port));
	}

}
