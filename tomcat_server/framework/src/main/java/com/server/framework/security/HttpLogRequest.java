package com.server.framework.security;

import com.server.framework.common.EntityType;

public class HttpLogRequest
{
	private String url, method, ip, queryString, jsonPayLoad, threadName;
	private EntityType entityType;
	private int statusCode;
	private boolean isOutgoing;
	private String requestHeaders;
	private String responseHeaders;

	private HttpLogRequest(Builder builder)
	{
		this.url = builder.url;
		this.method = builder.method;
		this.ip = builder.ip;
		this.queryString = builder.queryString;
		this.jsonPayLoad = builder.jsonPayLoad;
		this.threadName = builder.threadName;
		this.entityType = builder.entityType;
		this.statusCode = builder.statusCode;
		this.isOutgoing = builder.isOutgoing;
		this.requestHeaders = builder.requestHeaders;
		this.responseHeaders = builder.responseHeaders;
	}

	public String getUrl()
	{
		return url;
	}

	public String getMethod()
	{
		return method;
	}

	public String getIP()
	{
		return ip;
	}

	public String getQueryString()
	{
		return queryString;
	}

	public String getJsonPayLoad()
	{
		return jsonPayLoad;
	}

	public String getThreadName()
	{
		return threadName;
	}

	public EntityType getEntityType()
	{
		return entityType;
	}

	public int getStatusCode()
	{
		return statusCode;
	}

	public boolean isOutgoing()
	{
		return isOutgoing;
	}

	public String getRequestHeaders()
	{
		return requestHeaders;
	}

	public String getResponseHeaders()
	{
		return responseHeaders;
	}

	public static class Builder
	{
		private String url, method, ip, queryString, jsonPayLoad, threadName = Thread.currentThread().getName();
		private EntityType entityType = EntityType.COMMON;
		private int statusCode;
		private boolean isOutgoing;
		private String requestHeaders;
		private String responseHeaders;

		public Builder setUrl(String url)
		{
			this.url = url;
			return this;
		}

		public Builder setMethod(String method)
		{
			this.method = method;
			return this;
		}

		public Builder setIP(String ip)
		{
			this.ip = ip;
			return this;
		}

		public Builder setQueryString(String queryString)
		{
			this.queryString = queryString;
			return this;
		}

		public Builder setJsonPayLoad(String jsonPayLoad)
		{
			this.jsonPayLoad = jsonPayLoad;
			return this;
		}

		public Builder setThreadName(String threadName)
		{
			this.threadName = threadName;
			return this;
		}

		public Builder setEntityType(EntityType entityType)
		{
			this.entityType = entityType;
			return this;
		}

		public Builder setStatusCode(int statusCode)
		{
			this.statusCode = statusCode;
			return this;
		}

		public Builder setOutgoing(boolean outgoing)
		{
			isOutgoing = outgoing;
			return this;
		}

		public Builder setRequestHeaders(String requestHeaders)
		{
			this.requestHeaders = requestHeaders;
			return this;
		}

		public Builder setResponseHeaders(String responseHeaders)
		{
			this.responseHeaders = responseHeaders;
			return this;
		}

		public HttpLogRequest build()
		{
			return new HttpLogRequest(this);
		}
	}

}
