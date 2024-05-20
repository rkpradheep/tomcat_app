package com.server.framework.persistence;

import java.util.ArrayList;
import java.util.List;

public class SelectQuery
{
	String tableName;
	Criteria criteria;
	List<Join> joinList = new ArrayList<>();
	boolean needLock = false;

	public static class Join
	{
		String baseTableName;
		String baseTableColumnName;
		String referenceTableName;
		String referenceTableColumnName;
		String joinType;

		public Join(String baseTableName, String baseTableColumnName, String referenceTableName, String referenceTableColumnName)
		{
			this.baseTableName = baseTableName;
			this.baseTableColumnName = baseTableColumnName;
			this.referenceTableName = referenceTableName;
			this.referenceTableColumnName = referenceTableColumnName;
		}

		public static class Constants
		{
			public static final String INNER_JOIN = "INNER JOIN";
			public static final String LEFT_JOIN = "LEFT JOIN";
			public static final String RIGHT_JOIN = "RIGHT JOIN";
			public static final String FULL_JOIN = "FULL JOIN";
		}
	}

	public SelectQuery(String tableName)
	{
		this.tableName = tableName;
	}

	public void setCriteria(Criteria criteria)
	{
		this.criteria = criteria;
	}

	public void setLock()
	{
		this.needLock = true;
	}

	public void addJoin(Join join, String joinType)
	{
		join.joinType = joinType;
		joinList.add(join);
	}
}
