package com.server.stats.meta;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;

public class StatsMeta
{
	private String requestId;
	private String method;
	private Map<String, String> params = new HashMap<>();
	private final Map<String, String> requestHeaders = new HashMap<>();
	private int requestBatchSize;
	private int requestIntervalSeconds;
	private BiFunction<String,Object,String> placeholderHandlerFunction;
	private AtomicInteger requestCount = new AtomicInteger(0);
	private PrintWriter responseWriter;
	private FileWriter rawResponseWriter;
	private String requestFilePath;
	private Reader  requestDataReader;
	private String responseFilePath;
	private RequestMeta requestMeta;
	private String responseHeaders;
	private final ResponseMeta responseMeta = new ResponseMeta();
	private boolean isTest;


	public String getMethod()
	{
		return method;
	}

	public void setMethod(String method)
	{
		this.method = method;
	}

	public Map<String, String> getParams()
	{
		return params;
	}


	public void addRequestHeader(String headerName, String headerValue)
	{
		requestHeaders.put(headerName, headerValue);
	}

	public Map<String,String> getRequestHeaders()
	{
		return requestHeaders;
	}

	public int getRequestBatchSize()
	{
		return requestBatchSize;
	}

	public void setRequestBatchSize(int requestBatchSize)
	{
		this.requestBatchSize = requestBatchSize;
	}

	public int getRequestIntervalSeconds()
	{
		return requestIntervalSeconds;
	}

	public void setRequestIntervalSeconds(int requestIntervalSeconds)
	{
		this.requestIntervalSeconds = requestIntervalSeconds;
	}

	public BiFunction<String,Object, String> getPlaceholderHandlerFunction()
	{
		return placeholderHandlerFunction;
	}

	public void setPlaceholderHandlerFunction(BiFunction<String,Object,String> placeholderHandlerFunction)
	{
		this.placeholderHandlerFunction = placeholderHandlerFunction;
	}

	public int getRequestCount()
	{
		return requestCount.get();
	}

	public void incrementCount()
	{
		this.requestCount.incrementAndGet();
	}

	public PrintWriter getResponseWriter()
	{
		return responseWriter;
	}

	public void setResponseWriter(PrintWriter responseWriter)
	{
		this.responseWriter = responseWriter;
	}


	public String getResponseFilePath()
	{
		return responseFilePath;
	}

	public void setResponseFilePath(String responseFilePath)
	{
		this.responseFilePath = responseFilePath;
	}

	public String getRequestFilePath()
	{
		return requestFilePath;
	}

	public void setRequestFilePath(String requestFilePath)
	{
		this.requestFilePath = requestFilePath;
	}

	public RequestMeta getRequestMeta()
	{
		return requestMeta;
	}

	public void setRequestMeta(RequestMeta requestMeta)
	{
		this.requestMeta = requestMeta;
	}

	public String getResponseHeaders()
	{
		return responseHeaders;
	}

	public void setResponseHeaders(String responseHeaders)
	{
		this.responseHeaders = responseHeaders;
	}

	public void addResponseColumn(String columnName, String columnValue, boolean isPlaceHolder)
	{
		responseMeta.addResponseColumn(columnName, columnValue, isPlaceHolder);
	}

	public List<String> getResponseColumnNames()
	{
		return responseMeta.getResponseColumns();
	}

	public String getResponseColumnValue(String columnName)
	{
		return responseMeta.getColumnValue(columnName);
	}

	public boolean isPHResponseColumn(String columnName)
	{
		return responseMeta.isPHColumn(columnName);
	}

	public Reader getRequestDataReader()
	{
		return requestDataReader;
	}

	public void setRequestDataReader(Reader requestDataReader)
	{
		this.requestDataReader = requestDataReader;
	}

	public FileWriter getRawResponseWriter()
	{
		return rawResponseWriter;
	}

	public void setRawResponseWriter(FileWriter rawResponseWriter)
	{
		this.rawResponseWriter = rawResponseWriter;
	}

	public String getRequestId()
	{
		return requestId;
	}

	public void setRequestId(String requestId)
	{
		this.requestId = requestId;
	}

	public boolean isTest()
	{
		return isTest;
	}

	public void setTest(boolean test)
	{
		isTest = test;
	}
}
