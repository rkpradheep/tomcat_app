package com.server.framework.user;

public class User
{
	private Long id;
	private String name;
	private boolean isAdmin;

	public User(Long id, String name, boolean isAdmin)
	{
		this.id = id;
		this.name = name;
		this.isAdmin = isAdmin;
	}

	public Long getId()
	{
		return id;
	}

	public String getName()
	{
		return name;
	}

	public boolean isAdmin()
	{
		return isAdmin;
	}
}
