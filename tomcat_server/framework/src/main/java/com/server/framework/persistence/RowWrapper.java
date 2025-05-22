package com.server.framework.persistence;

import com.server.framework.persistence.Row;

class RowWrapper extends Row
{

	public RowWrapper(String tableName)
	{
		super(tableName);
	}

	@Override public void set(String columnAlias, Object value)
	{
		rowMap.put(columnAlias, value);
	}
	@Override public Object get(String columnAlias)
	{
		return rowMap.getOrDefault(columnAlias, super.get(columnAlias));
	}
}
