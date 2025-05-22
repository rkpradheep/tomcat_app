package com.server.framework.persistence;

import java.util.List;

import com.server.framework.persistence.Column;
import com.server.framework.persistence.Criteria;

public class GroupByClause
{
	List<Column> columnList;
	Criteria criteria;

	public GroupByClause(List<Column> columnList, Criteria criteria)
	{
		this.columnList = columnList;
		this.criteria = criteria;
	}

	public GroupByClause(List<Column> columnList)
	{
		this(columnList, null);
	}
}
