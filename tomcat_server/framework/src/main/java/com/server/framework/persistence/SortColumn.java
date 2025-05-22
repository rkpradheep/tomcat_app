package com.server.framework.persistence;

import com.server.framework.persistence.Column;

public class SortColumn
{
	Column column;
	boolean isAscending;

	public SortColumn(Column column, boolean isAscending)
	{
		this.column = column;
		this.isAscending = isAscending;
	}
}
