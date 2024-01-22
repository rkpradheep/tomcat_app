package com.server.concurrency;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.json.JSONObject;

import com.server.common.Util;
import com.server.http.FormData;
import com.server.http.HttpAPI;
import com.server.http.HttpResponse;

public class ConcurrencyAPIHandler extends HttpServlet
{
	private static final Logger LOGGER = Logger.getLogger(ConcurrencyAPIHandler.class.getName());

	@Override
	public void service(HttpServletRequest request, HttpServletResponse response) throws IOException
	{
		Map<String, FormData> formDataMap = Util.parserMultiPartFormData(request);

		JSONObject jsonObject = new JSONObject(formDataMap.get("meta_json").getValue());
		int concurrencyCalls = jsonObject.getInt("concurrency_calls");

		if(concurrencyCalls < 0 || concurrencyCalls > 100)
		{
			Util.writerErrorResponse(response, "Concurrency calls range should be between 0 and 100");
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
		JSONObject headers = jsonObject.getJSONObject("headers");

		String queryString = parseQueryString(params);

		formDataMap.remove("meta_json");

		Map<String, String> headersMap = headers.keySet().stream().collect(Collectors.toMap(key -> key, headers::getString));

		List<Map> responseList = new ArrayList<>();
		List<Future<?>> futureList = new ArrayList<>();
		ExecutorService executorService = Executors.newFixedThreadPool(concurrencyCalls);
		AtomicInteger atomicInteger = new AtomicInteger(0);

		Runnable runnable = () -> {
			try
			{
				Map<String, String> finalHeadersMap = new HashMap<>(headersMap);
				HttpResponse httpResponse = HttpAPI.makeNetworkCall(url, method, queryString, finalHeadersMap, getInputStream(formDataMap, finalHeadersMap));
				StringWriter stringWriter = new StringWriter();
				IOUtils.copy(httpResponse.getInputStream(), stringWriter);
				JSONObject responseJSON = new JSONObject();
				Object responseObject = ObjectUtils.defaultIfNull(Util.getJSONFromString(stringWriter.toString()), stringWriter.toString());
				responseJSON.put("Response " + atomicInteger.incrementAndGet(), responseObject);
				synchronized(responseList)
				{
					responseList.add(responseJSON.toMap());
				}
			}
			catch(Exception e)
			{
				LOGGER.log(Level.SEVERE, "Exception occurred ", e);
			}
		};
		for(int i = 0; i < concurrencyCalls; i++)
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

		LOGGER.log(Level.INFO, "Response list size {0}", futureList.size());

		Util.writeJSONResponse(response, responseList);
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
			headersMap.put("Content-Type", "x-www-form-urlencoded");
			return new ByteArrayInputStream(parseQueryString(new JSONObject(formUrlEncodedJSONString)).getBytes());
		}

		if(formDataMap.size() < 1)
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

	static String parseQueryString(JSONObject params)
	{
		String parsedQueryString = StringUtils.EMPTY;

		for(String param : params.keySet())
		{
			parsedQueryString += URLEncoder.encode(param.trim()) + "=" + URLEncoder.encode(params.getString(param)) + "&";
		}
		return parsedQueryString.replaceAll("&$", StringUtils.EMPTY);
	}

}
