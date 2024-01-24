package com.server.admin;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import com.server.common.Util;
import com.server.db.DBUtil;

public class AdminDBHandler extends HttpServlet
{
	private static final Logger LOGGER = Logger.getLogger(AdminDBHandler.class.getName());

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws IOException
	{
		try
		{
			handleAdminDBRequest(request, response);
		}
		catch(Exception e)
		{
			LOGGER.log(Level.SEVERE, "Exception occurred", e);
			Util.writerErrorResponse(response, e.getMessage());
		}
	}

	private void handleAdminDBRequest(HttpServletRequest request, HttpServletResponse response) throws Exception
	{
		JSONObject credentials = Util.getJSONObject(request);

		String query = credentials.optString("query", "");

		boolean tableOrColumnRequest = credentials.optBoolean("need_table") || credentials.optBoolean("need_column");

		try(Connection conn = DBUtil.getServerDBConnection())
		{
			if(tableOrColumnRequest)
			{
				handleTableOrColumnMeta(conn, response, credentials);
				return;
			}

			Map<String, Object> finalResponse = new LinkedHashMap<>();
			AdminDBUtil.handleQuery(conn, query, finalResponse);
			Util.writeJSONResponse(response, finalResponse);
		}
	}

	private void handleTableOrColumnMeta(Connection connection, HttpServletResponse response, JSONObject credentials) throws Exception
	{

		if(credentials.optBoolean("need_table"))
		{
			DatabaseMetaData databaseMetaData = connection.getMetaData();
			//new Thread(() -> com.server.common.Util.postMessageToBot("Visitor Alert. \nIP : " + request.getServerName() + "\nSession ID : " + request.getSession().getId())).start();
			List<String> tableList = new ArrayList<>();
			ResultSet tableResultSet = databaseMetaData.getTables(null, DBUtil.schemaName, "%", new String[] {"TABLE"});
			while(tableResultSet.next())
			{
				try
				{
					if(!tableResultSet.getString("TABLE_NAME").equalsIgnoreCase("Table"))
						tableList.add(tableResultSet.getString("TABLE_NAME"));
				}
				catch(Exception e)
				{
				}
			}
			tableList.add("Throttle");
			Util.writeJSONResponse(response, tableList);
			return;
		}

		handleColumnMeta(connection, response, credentials.getString("table"));
	}

	private void handleColumnMeta(Connection connection, HttpServletResponse response, String table) throws Exception
	{
		if(StringUtils.equals(table, "Throttle"))
		{
			Map<String, Object> result = new HashMap<>();
			result.put("columns", Arrays.asList("IP", "URI", "REQUEST_COUNT", "TIME_FRAME_START"));
			result.put("pk", StringUtils.EMPTY);

			Util.writeJSONResponse(response, result);
			return;
		}
		DatabaseMetaData databaseMetaData = connection.getMetaData();

		Map<String, Object> result = new LinkedHashMap<>();
		List<String> columnList = new ArrayList<>();
		List<String> bigIntColumns = new ArrayList<>();
		String pkName = "";
		ResultSet columnResultSet = databaseMetaData.getColumns(null, DBUtil.schemaName, table, null);
		while(columnResultSet.next())
		{
			String columnName = columnResultSet.getString("COLUMN_NAME").toUpperCase();
			columnList.add(columnName);
			if(columnResultSet.getInt("DATA_TYPE") == Types.BIGINT)
			{
				bigIntColumns.add(columnName);
			}
		}
		if(!bigIntColumns.isEmpty())
		{
			ResultSet primaryKeys = connection.getMetaData().getPrimaryKeys(null, DBUtil.schemaName, table);

			while(primaryKeys.next())
			{
				String pk = primaryKeys.getString("COLUMN_NAME").toUpperCase();
				if(bigIntColumns.contains(pk))
				{
					pkName = pk;
					break;
				}
			}

		}
		result.put("columns", columnList);
		result.put("pk", pkName);

		Util.writeJSONResponse(response, result);
	}
}
