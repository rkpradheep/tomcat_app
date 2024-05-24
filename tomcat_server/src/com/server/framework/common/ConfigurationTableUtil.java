package com.server.framework.common;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.server.framework.persistence.Criteria;
import com.server.framework.persistence.DBUtil;
import com.server.framework.persistence.DataAccess;
import com.server.framework.persistence.Row;
import com.server.framework.persistence.SelectQuery;
import com.server.table.constants.CONFIGURATION;

public class ConfigurationTableUtil
{
	private static final Logger LOGGER = Logger.getLogger(ConfigurationTableUtil.class.getName());

	public static String getValue(String key)
	{
		try
		{
			SelectQuery selectQuery = new SelectQuery(CONFIGURATION.TABLE);

			Criteria criteria = new Criteria(CONFIGURATION.TABLE, CONFIGURATION.CKEY, key, Criteria.Constants.EQUAL);
			selectQuery.setCriteria(criteria);

			return (String) DataAccess.get(selectQuery).getRows().get(0).get(CONFIGURATION.CVALUE);
		}
		catch(Exception e)
		{
			LOGGER.log(Level.SEVERE, "Exception occurred", e);
			return null;
		}
	}

	public static void setValue(String key, String value) throws Exception
	{
		Row row = new Row(CONFIGURATION.TABLE);
		row.set(CONFIGURATION.CKEY, key);
		row.set(CONFIGURATION.CVALUE, value);

		DataAccess.add(row);
	}

	public static void delete(String key) throws Exception
	{
		Criteria criteria = new Criteria(CONFIGURATION.TABLE, CONFIGURATION.CKEY, key, Criteria.Constants.EQUAL);

		DataAccess.delete(CONFIGURATION.TABLE, criteria);
	}
}
