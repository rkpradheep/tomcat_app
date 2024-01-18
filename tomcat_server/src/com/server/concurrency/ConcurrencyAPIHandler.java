package com.server.concurrency;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import com.server.common.Util;
import com.server.http.HttpAPI;
import com.server.http.HttpResponse;

public class ConcurrencyAPIHandler extends HttpServlet
{
	private static final Logger LOGGER = Logger.getLogger(ConcurrencyAPIHandler.class.getName());

	@Override
	public void service(HttpServletRequest request, HttpServletResponse response) throws IOException
	{
		JSONObject jsonObject = Util.getJSONObject(request);
		int concurrencyCalls = jsonObject.getInt("concurrency_calls");
		String url = jsonObject.getString("url");
		String method = jsonObject.getString("method");
		String queryString = jsonObject.getString("query_string");
		String jsonObjectString = jsonObject.getJSONObject("json").toString();
		JSONObject headers = jsonObject.getJSONObject("headers");

		Map<String, String> headersMap = headers.keySet().stream().collect(Collectors.toMap(key -> key, headers::getString));

		List<Map> responseList = new ArrayList<>();
		List<Future<?>> futureList = new ArrayList<>();
		ExecutorService executorService = Executors.newFixedThreadPool(concurrencyCalls);
		AtomicInteger atomicInteger = new AtomicInteger(0);

		Runnable runnable = () -> {
			try
			{
				InputStream inputStream = new ByteArrayInputStream(jsonObjectString.getBytes());
				HttpResponse httpResponse = HttpAPI.makeNetworkCall(url, method, queryString, headersMap, inputStream);
				StringWriter stringWriter = new StringWriter();
				IOUtils.copy(httpResponse.getInputStream(), stringWriter);
				JSONObject responseJSON = new JSONObject();
				Object responseObject = Util.isValidJSON(stringWriter.toString()) ? new JSONObject(stringWriter.toString()) : stringWriter.toString();
				responseJSON.put("Response " + atomicInteger.incrementAndGet(), responseObject);
				responseList.add(responseJSON.toMap());
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
		Util.writeJSONResponse(response, responseList);
	}

}
