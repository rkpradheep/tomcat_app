package com.server.zoho;

import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
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
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.server.framework.common.AppException;
import com.server.framework.common.Configuration;
import com.server.framework.common.Util;
import com.server.framework.security.SecurityUtil;

public class SASHandler extends HttpServlet
{
	private static final Logger LOGGER = Logger.getLogger(SASHandler.class.getName());

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws IOException
	{
		try
		{
			if(request.getRequestURI().contains("/api/v1/sas/meta"))
			{
				getSasMeta(request, response);
			}
			else if(request.getRequestURI().contains("/api/v1/sas/services"))
			{
				fetchServices(response);
			}
			else if(request.getRequestURI().contains("/api/v1/sas/execute"))
			{
				handleSasRequest(request, response);
			}
			else
			{
				Long pathParamValue = Long.valueOf(request.getRequestURI().substring(request.getRequestURI().lastIndexOf("/") + 1));

				if(!Boolean.parseBoolean(request.getParameter("pk")))
				{
					long[] limits = SASUtil.getLimits(pathParamValue);

					Map<String, Long> responseMap = new LinkedHashMap<>();
					responseMap.put("lower_limit", limits[0]);
					responseMap.put("upper_limit", limits[1]);

					SecurityUtil.writeJSONResponse(response, responseMap);
				}
				else
				{
					response.getWriter().println("Space ID : " + SASUtil.getSpaceIDFromPK(pathParamValue));
				}
			}
		}
		catch(Exception e)
		{
			LOGGER.log(Level.SEVERE, "Exception occurred", e);
			SecurityUtil.writerErrorResponse(response, e.getMessage());
		}
	}

	public static Map<String, Object> handleSasRequest(JSONObject credentials) throws Exception
	{
		String server = credentials.getString("server");
		String ip = credentials.getString("ip");
		String user = credentials.getString("user");
		String password = credentials.getString("password");
		String zsid = credentials.optString("zsid");
		String pk = credentials.optString("pk");
		String query = credentials.optString("query", "");

		try(Connection conn = SASUtil.getDBConnection(server, ip, "jbossdb", user, password))
		{
			zsid = !zsid.equals("") ? zsid : SASUtil.getZSIDFromPK(conn, pk);
			PreparedStatement statement = conn.prepareStatement("SELECT SASAccounts.ID, CustomerDatabase.SCHEMANAME, CustomerDatabase.DBMASTERID FROM SASAccounts INNER JOIN UserDomains on SASAccounts.ID = UserDomains.ID INNER JOIN CustomerDatabase on UserDomains.CUSTOMERID = CustomerDatabase.CustomerID where SASAccounts.LOGINNAME = ?");
			statement.setString(1, zsid);
			statement.execute();
			ResultSet resultSet = statement.getResultSet();
			boolean exist = resultSet.next();
			if(!exist)
			{
				throw new AppException("Invalid zsid or pk");
			}
			long[] limits = SASUtil.getLimits(Long.valueOf(resultSet.getString("ID")));
			String schema = resultSet.getString("SCHEMANAME");

			Map<String, Object> responseMap = new LinkedHashMap<>();

			responseMap.put("schema_name", schema);
			responseMap.put("zsid", zsid);

			String clusterIP = SASUtil.getClusterIP(conn, resultSet.getString("DBMASTERID"));
			responseMap.put("cluster_ip", clusterIP);

			Map<String, Object> limitsDetails = new LinkedHashMap<>();

			limitsDetails.put("sas_start_range", limits[0]);
			limitsDetails.put("sas_end_range", limits[1]);

			responseMap.put("sas_limits", limitsDetails);

			//com.server.framework.common.Util.addUserDetails(server, clusterIP, schema, user, password, responseMap, (Long) limits[0], (Long) limits[1]);

			Map<String, Object> finalResponse = new LinkedHashMap<>();
			finalResponse.put("sas_meta", responseMap);
			SASUtil.handleQuery(query, server, clusterIP, schema, user, password, finalResponse, limits[0], limits[1]);

			return finalResponse;
		}
	}

	private void handleSasRequest(HttpServletRequest request, HttpServletResponse response) throws Exception
	{
		JSONObject credentials = SecurityUtil.getJSONObject(request);

		boolean is_encrypted = credentials.optBoolean("is_encrypted");

		if(is_encrypted)
		{
			SASUtil.handleDecryption(request, credentials);
		}

		boolean tableOrColumnRequest = credentials.optBoolean("need_table") || credentials.optBoolean("need_column");

		if(tableOrColumnRequest)
		{
			handleTableOrColumnMeta(response, credentials);
			return;
		}

		SecurityUtil.writeJSONResponse(response, handleSasRequest(credentials));
	}

	private void handleTableOrColumnMeta(HttpServletResponse response, JSONObject credentials) throws Exception
	{
		String server = credentials.getString("server");
		String ip = credentials.getString("ip");
		String user = credentials.getString("user");
		String password = credentials.getString("password");

		try(Connection connection = SASUtil.getDBConnection(server, ip, "jbossdb", user, password))
		{
			if(credentials.optBoolean("need_table"))
			{
				DatabaseMetaData databaseMetaData = connection.getMetaData();
				//new Thread(() -> com.server.framework.common.Util.postMessageToBot("Visitor Alert. \nIP : " + request.getServerName() + "\nSession ID : " + request.getSession().getId())).start();
				List<String> tableList = new ArrayList<>();
				ResultSet tableResultSet = databaseMetaData.getTables(null, "jbossdb", "%", new String[] {"TABLE"});
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
				SecurityUtil.writeJSONResponse(response, tableList);
				return;
			}

			handleColumnMeta(connection, response, credentials.getString("table"));
		}
	}

	private void handleColumnMeta(Connection connection, HttpServletResponse response, String table) throws Exception
	{
		DatabaseMetaData databaseMetaData = connection.getMetaData();

		Map<String, Object> result = new LinkedHashMap<>();
		List<String> columnList = new ArrayList<>();
		List<String> bigIntColumns = new ArrayList<>();
		String pkName = "";
		ResultSet columnResultSet = databaseMetaData.getColumns(null, "jbossdb", table, null);
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
			ResultSet primaryKeys = connection.getMetaData().getPrimaryKeys(null, "jbossdb", table);

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

	private void getSasMeta(HttpServletRequest request, HttpServletResponse response) throws Exception
	{

		PrivateKey privateKey = (PrivateKey) request.getSession().getAttribute("private_key");
		PublicKey publicKey = (PublicKey) request.getSession().getAttribute("public_key");

		if(privateKey == null)
		{
			KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
			SecureRandom random = SecureRandom.getInstanceStrong();
			keyPairGen.initialize(2048, random);
			KeyPair keyPair = keyPairGen.generateKeyPair();

			privateKey = keyPair.getPrivate();
			publicKey = keyPair.getPublic();

			request.getSession().setAttribute("private_key", privateKey);
			request.getSession().setAttribute("public_key", publicKey);

			StringBuilder header = new StringBuilder();
			header.append("JSESSIONID=").append(request.getSession().getId());
			header.append("; Secure");
			header.append("; SameSite=None");
			response.setHeader("Set-Cookie", header.toString());
		}

		Map<String, Object> map = new HashMap<>();

		for(String service : Configuration.getProperty("db.services").split(","))
		{
			Map<String, String> serviceCredentials = new HashMap<>();

			String ip = Configuration.getProperty("db.$.ip".replace("$", service));
			String user = Configuration.getProperty("db.$.user".replace("$", service));
			String password = Configuration.getProperty("db.$.password".replace("$", service));

			serviceCredentials.put("ip", Util.encryptData(publicKey, ip));
			serviceCredentials.put("user", Util.encryptData(publicKey, user));
			serviceCredentials.put("password", Util.encryptData(publicKey, password));

			map.put(service, serviceCredentials);
		}

		Map<String, String> iscMeta = new HashMap<>();

		for(String service : Configuration.getProperty("security.services").split(","))
		{
			iscMeta.put(service, Configuration.getProperty("zoho." + service + ".display.name"));
		}

		map.put("isc_meta", iscMeta);

		Map<String, String> earMeta = new HashMap<>();

		for(String service : Configuration.getProperty("ear.services").split(","))
		{
			earMeta.put(service, Configuration.getProperty("zoho." + service + ".display.name"));
		}

		map.put("ear_meta", earMeta);


		Map<String, Object> taskEngineMeta = new HashMap<>();

		Map<String, Object> products = new HashMap<>();
		for(String service : Configuration.getProperty("taskengine.services").split(","))
		{
			products.put(service, Configuration.getProperty("zoho." + service + ".display.name"));
		}

		taskEngineMeta.put("products", products);

		Map<String, List<String>> threadPoolMeta = new HashMap<>();

		for(String service : Configuration.getProperty("taskengine.services").split(","))
		{
			threadPoolMeta.put(service.split("-")[0], Arrays.stream(Configuration.getProperty("taskengine." + service.split("-")[0] + ".threadpools").split(",")).collect(Collectors.toList()));
		}

		taskEngineMeta.put("thread_pools", threadPoolMeta);

		map.put("taskengine_meta", taskEngineMeta);

		SecurityUtil.writeJSONResponse(response, map);

	}

	private static void fetchServices(HttpServletResponse response) throws IOException
	{
		String[] servicesNames = Configuration.getProperty("db.services").split(",");

		Map<String, Map<String, String>> responseMap = new HashMap<>();

		for(String service : servicesNames)
		{
			Map<String, String> serviceDetails = new HashMap<>();
			serviceDetails.put("display_name", Configuration.getProperty("zoho.$.display.name".replace("$", service)));
			serviceDetails.put("server", Configuration.getProperty("db.$.server".replace("$", service)));

			responseMap.put(service, serviceDetails);
		}
		SecurityUtil.writeJSONResponse(response, responseMap);
	}

}
