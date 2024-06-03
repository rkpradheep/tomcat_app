package com.server.framework.persistence;

public class Column
{
	String tableName;
	String columnName;

	private Column(String tableName, String columnName)
	{
		this.tableName = tableName;
		this.columnName = columnName;
	}

	public static Column getColumn(String tableName, String columnName)
	{
		return new Column(tableName, columnName);
	}
}
