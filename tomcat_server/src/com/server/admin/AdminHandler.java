package com.server.admin;

import java.io.File;
import java.io.FileOutputStream;
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
import com.server.security.DBUtil;
import com.server.security.LoginUtil;
import com.server.security.SecurityUtil;
import com.server.security.ThrottleHandler;
import com.server.security.http.FormData;

public class AdminHandler extends HttpServlet
{
	private static final Logger LOGGER = Logger.getLogger(AdminHandler.class.getName());

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws IOException
	{
		try
		{
			if(request.getRequestURI().contains("/db/execute"))
			{
				handleAdminDBRequest(request, response);
			}
			else if(request.getRequestURI().contains("/delete/expired"))
			{
				ThrottleHandler.removeExpiredIPLockingAndThrottleMeta();
				LoginUtil.deleteExpiredSessions();

				SecurityUtil.writeSuccessJSONResponse(response, "Deleted successfully");
			}
			else if(request.getRequestURI().contains("/file/transfer"))
			{
				handleFileTransfer(request, response);
			}
		}
		catch(Exception e)
		{
			LOGGER.log(Level.SEVERE, "Exception occurred", e);
			SecurityUtil.writerErrorResponse(response, e.getMessage());
		}
	}

	private void handleFileTransfer(HttpServletRequest request, HttpServletResponse response) throws IOException
	{
		Map<String, FormData> formDataMap = SecurityUtil.parseMultiPartFormData(request);
		String path = formDataMap.get("path").getValue();
		path = path.replaceAll("/*$", StringUtils.EMPTY);
		File file = new File(Util.HOME_PATH, path);
		if(!file.exists())
		{
			SecurityUtil.writerErrorResponse(response, "Invalid path");
			return;
		}
		FormData.FileData fileData = formDataMap.get("file").getFileDataList().get(0);
		file = new File(Util.HOME_PATH, path + "/" + fileData.getFileName());
		try(FileOutputStream fileOutputStream = new FileOutputStream(file))
		{
			fileOutputStream.write(fileData.getBytes());
		}

		SecurityUtil.writeSuccessJSONResponse(response, "File transferred successfully");
	}

	private void handleAdminDBRequest(HttpServletRequest request, HttpServletResponse response) throws Exception
	{
		JSONObject credentials = SecurityUtil.getJSONObject(request);

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
			SecurityUtil.writeJSONResponse(response, finalResponse);
		}
	}

	private void handleTableOrColumnMeta(Connection connection, HttpServletResponse response, JSONObject credentials) throws Exception
	{

		if(credentials.optBoolean("need_table"))
		{
			DatabaseMetaData databaseMetaData = connection.getMetaData();
			//new Thread(() -> com.server.common.Util.postMessageToBot("Visitor Alert. \nIP : " + request.getServerName() + "\nSession ID : " + request.getSession().getId())).start();
			List<String> tableList = new ArrayList<>();
			try
			{
				ResultSet rs = connection.createStatement().executeQuery("Show tables");
				while(rs.next())
				{
					tableList.add(rs.getString(1));
				}
			}
			catch(Exception e)
			{
			}

			tableList.add("Throttle");
			SecurityUtil.writeJSONResponse(response, tableList);
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

			SecurityUtil.writeJSONResponse(response, result);
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

		SecurityUtil.writeJSONResponse(response, result);
	}
}
