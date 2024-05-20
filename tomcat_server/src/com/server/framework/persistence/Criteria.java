package com.server.framework.persistence;

public class Criteria
{
	public static class Constants
	{
		public static final String EQUAL = "=";
		public static final String NOT_EQUAL = "!=";
		public static final String LIKE = "LIKE";
		public static final String IN = "IN";
		public static final String NOT_IN = "NOT IN";
		public static final String AND = "AND";
		public static final String OR = "OR";
	}

	Criteria leftCriteria;
	Criteria rightCriteria;
	Criterion criterion;
	String operator;

	static class Criterion
	{
		String tableName;
		String columnName;
		Object columnValue;
		String comparator;

		private Criterion(String tableName, String columnName, Object value, String comparator)
		{
			this.tableName = tableName;
			this.columnName = columnName;
			this.columnValue = value;
			this.comparator = comparator;
		}
	}

	public Criteria(String tableName, String columnName, Object columnValue, String comparator)
	{
		this.criterion = new Criterion(tableName, columnName, columnValue, comparator);
		this.leftCriteria = null;
		this.rightCriteria = null;
	}

	private Criteria()
	{
		this.criterion = null;
		this.leftCriteria = null;
		this.rightCriteria = null;
	}

	public Criteria and(Criteria criteria)
	{
		Criteria cr = new Criteria();

		cr.leftCriteria = this;

		cr.operator = "and";

		cr.rightCriteria = criteria;

		return cr;
	}

	public Criteria or(Criteria criteria)
	{
		Criteria cr = new Criteria();

		cr.leftCriteria = this;

		cr.operator = "or";

		cr.rightCriteria = criteria;

		return cr;
	}

}