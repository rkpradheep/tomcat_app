package com.server.security.user;

public class User
{
	private Long id;
	private String name;
	private String sessionId;
	private Long expiryTime;

	public User(Long id, String name, String sessionId, long expiryTime)
	{
		this.id = id;
		this.name = name;
		this.sessionId = sessionId;
		this.expiryTime = expiryTime;
	}

	public Long getId()
	{
		return id;
	}

	public String getName()
	{
		return name;
	}

	public String getSessionId()
	{
		return sessionId;
	}

	public Long getExpiryTime()
	{
		return expiryTime;
	}
}
