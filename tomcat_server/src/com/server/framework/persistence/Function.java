package com.server.framework.persistence;

public class Function
{
	String name;
	Object[] args;

	private Function(String name, Object... args)
	{
		this.name = name;
		this.args = args;
	}

	public static Function createFunction(String functionName, Object... args)
	{
		return new Function(functionName, args);
	}

	public static final class Constants
	{
		public static final String UPPER = "UPPER";
		public static final String LOWER = "LOWER";
		public static final String POW = "POW";
	}
}
