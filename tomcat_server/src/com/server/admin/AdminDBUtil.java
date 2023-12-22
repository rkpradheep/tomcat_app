package com.server.admin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

public class AdminDBUtil
{
	public static void handleQuery(Connection connection, String query, Map<String, Object> resultMap) throws SQLException
	{
		if(StringUtils.isBlank(StringUtils.deleteWhitespace(query)))
		{
			return;
		}
		query = query.replaceAll("\n", StringUtils.SPACE);
		query = query.replaceAll("([.;])$", StringUtils.EMPTY);

		PreparedStatement preparedStatement;

		Matcher orderByMatcher = Pattern.compile("(.*)(?i)(order)\\s+(?i)(by)(.*)").matcher(query);
		Matcher groupByMatcher = Pattern.compile("(.*)(?i)(group)\\s+(?i)(by)(.*)").matcher(query);
		Matcher havingMatcher = Pattern.compile("(.*)(?i)(having)(.*)").matcher(query);
		Matcher limitMatcher = Pattern.compile("(.*)(?i)(limit)(.*)").matcher(query);

		String startQuery = query;
		String endQuery = "";
		if(orderByMatcher.matches())
		{
			startQuery = query.substring(0, orderByMatcher.start(2));
			endQuery = query.substring(orderByMatcher.start(2));
		}
		else if(groupByMatcher.matches())
		{
			startQuery = query.substring(0, groupByMatcher.start(2));
			endQuery = query.substring(groupByMatcher.start(2));
		}
		else if(havingMatcher.matches())
		{
			startQuery = query.substring(0, havingMatcher.start(2));
			endQuery = query.substring(havingMatcher.start(2));
		}
		else if(limitMatcher.matches())
		{
			startQuery = query.substring(0, limitMatcher.start(2));
			endQuery = query.substring(limitMatcher.start(2));
		}

		preparedStatement = connection.prepareStatement(startQuery + endQuery);

		preparedStatement.execute();

		if(!query.matches("(?i)select(.*)"))
		{
			resultMap.put("query_output", "Executed successfully");
			return;
		}
		ResultSet resultSet = preparedStatement.getResultSet();
		ResultSetMetaData resultSetMetaData = resultSet.getMetaData();

		List<Map<String, String>> queryOutput = new ArrayList<>();
		while(resultSet.next())
		{
			Map<String, String> row = new LinkedHashMap<>();
			for(int i = 1; i <= resultSetMetaData.getColumnCount(); i++)
			{
				row.put(resultSetMetaData.getColumnName(i).toUpperCase(), resultSet.getString(i));
			}
			queryOutput.add(row);
		}
		if(queryOutput.isEmpty())
		{
			Map<String, String> row = new LinkedHashMap<>();
			for(int i = 1; i <= resultSetMetaData.getColumnCount(); i++)
			{
				row.put(resultSetMetaData.getColumnName(i).toUpperCase(), "<EMPTY>");
			}
			queryOutput.add(row);
		}
		resultMap.put("query_output", queryOutput);

	}
}
