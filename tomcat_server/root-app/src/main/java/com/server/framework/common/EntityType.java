package com.server.framework.common;

public enum EntityType
{
	COMMON(1),
	ZOHO(2);
	final int value;

	EntityType(int value)
	{
		this.value = value;
	}

	public int getValue()
	{
		return value;
	}
}
