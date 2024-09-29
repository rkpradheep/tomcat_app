package com.server.zoho;

import java.io.BufferedWriter;
import java.io.File;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.Connection;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import com.server.framework.common.AppException;
import com.server.framework.common.DateUtil;
import com.server.framework.common.Util;
import com.server.framework.job.JobUtil;
import com.server.framework.security.SecurityUtil;

public class StatsQueryTool
{
	private static final Logger LOGGER = Logger.getLogger(StatsQueryTool.class.getName());

	private static List<Map<String, String>> getSchemas(Connection connection) throws Exception
	{
		String schemaQuery = "select GridAddress.ADDRESS as ip ,CustomerDatabase.SCHEMANAME as schema_name from CustomerDatabase inner join DBCluster on DBCluster.DBCLUSTERID=CustomerDatabase.DBMASTERID inner join GridServices on GridServices.SERVICEID = DBCluster.DBCLUSTERID inner join GridAccount on GridAccount.ACCOUNTID = GridServices.ACCOUNTID inner join GridAddress on GridAddress.ADDRESSID=GridAccount.ADDRESSID";
		return executeSelect(schemaQuery, connection);
	}

	public static Map<String, String> handleStats(JSONObject credentials) throws Exception
	{
		String server = credentials.getString("server");
		String ip = credentials.getString("ip");
		String user = credentials.getString("user");
		String password = credentials.getString("password");
		String query = credentials.getString("query").trim().replaceAll("\n", StringUtils.EMPTY);

		Pattern selectQuerPattern = Pattern.compile("(?i)(select)(.*)(from)\\s+(\\w+)(.*)");
		Matcher matcher = selectQuerPattern.matcher(query);
		if(!matcher.matches())
		{
			throw new AppException("Not a valid Select query");
		}

		String reqId = String.valueOf(Math.abs(query.hashCode()));

		File file = new File(SecurityUtil.getUploadsPath() + "/" + reqId + "_inprocess.csv");
		if(file.exists())
		{
			throw new AppException("Stats is already in running state for this query");
		}

		Connection mainClusterMasterConnection = SASUtil.getDBConnection(server, ip, "jbossdb", user, password);
		mainClusterMasterConnection.createStatement().execute(query);

		String mainClusterSlaveIP = SASUtil.getMasterSlaveIPPair(mainClusterMasterConnection, ip).getRight();

		file.createNewFile();

		mainClusterMasterConnection.close();

		JobUtil.scheduleJob(() -> {

			PrintWriter output = null;
			try(Connection mainClusterSlaveConnection = SASUtil.getDBConnection(server, mainClusterSlaveIP, "jbossdb", user, password))
			{
				long startTime = DateUtil.getCurrentTimeInMillis();

				boolean isHeaderWritten = false;
				output = new PrintWriter(new BufferedWriter(new FileWriter(file)));
				List<Map<String, String>> schemaMeta = getSchemas(mainClusterSlaveConnection);
				LOGGER.log(Level.INFO, "Total number of schemas : {0}", schemaMeta.size());
				int schemaCount = 0;
				for(Map<String, String> schemaMap : schemaMeta)
				{
					LOGGER.log(Level.INFO, "Number of schemas yet to be run {0}", schemaMeta.size() - schemaCount);

					schemaCount++;
					String host = schemaMap.get("IP");
					String schema = schemaMap.get("SCHEMA_NAME");

					try(Connection slaveConnection = SASUtil.getDBConnection(server, SASUtil.getMasterSlaveIPPair(mainClusterSlaveConnection, host).getValue(), schema, user, password))
					{
						StringBuilder stringBuilder = new StringBuilder();

						List<Map<String, String>> outputList = executeSelect(query, slaveConnection);
						for(Map<String, String> outputMap : outputList)
						{
							if(!isHeaderWritten)
							{
								stringBuilder.append(String.join(",", outputMap.keySet())).append(",ClusterIP,").append("SchemaName").append("\n");
								isHeaderWritten = true;
							}
							if(outputMap.containsValue("<EMPTY>"))
							{
								break;
							}
							stringBuilder.append(String.join(",", outputMap.values())).append(",").append(host).append(",").append(schema).append("\n");
						}
						output.print(stringBuilder);
						output.flush();
					}
					catch(Exception e)
					{
						LOGGER.log(Level.SEVERE, "Exception occurred", e);
					}
				}

				LOGGER.log(Level.INFO, "Stats completed for RequestID {0} in {1} seconds", new Object[] {reqId, ((DateUtil.getCurrentTimeInMillis() - startTime) / 1000f)});

			}
			finally
			{
				File destFile = new File(file.getAbsolutePath().replace("_inprocess", ""));
				file.renameTo(destFile);
				IOUtils.copy(new FileInputStream(destFile), new FileOutputStream(Util.HOME_PATH + "/uploads/" + destFile.getName()));
				if(output != null)
				{
					output.close();
				}
			}

		}, 1);

		return Map.of("message", "Stats request initiated successfully with RequestId : " + reqId);
	}

	private static List<Map<String, String>> executeSelect(String sqlQuery, Connection connection) throws Exception
	{
		PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
		preparedStatement.execute();

		return SASUtil.getQueryOutput(preparedStatement);
	}
}
