package com.server.zoho;

import java.security.PrivateKey;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONObject;

import com.server.framework.common.AppException;
import com.server.framework.common.Configuration;
import com.server.framework.common.Util;

public class SASUtil
{
	private static final ThreadLocal<Integer> CONNECTION_RETRY_ATTEMPT = ThreadLocal.withInitial(() -> 0);

	public static long[] getLimits(Long spaceID)
	{
		long rangeId = spaceID % 9000000L;

		long startId = (rangeId - 1L) * 1000000000000L;
		long endId = rangeId * 1000000000000L - 1L;
		return new long[] {startId, endId};
	}

	public static String getSpaceIDFromPK(Long pk)
	{
		pk = (pk / 1000000000000L) + 1;

		String spaceID = pk.toString();
		return spaceID.length() == 5 ? "90".concat(spaceID) : spaceID.length() == 6 ? "9".concat(spaceID) : spaceID;
	}

	public static String getZSIDFromPK(Connection connection, String pk) throws SQLException
	{
		PreparedStatement statement = connection.prepareStatement("SELECT SASAccounts.LOGINNAME from SASAccounts where SASAccounts.ID = ?");
		statement.setLong(1, Long.parseLong(getSpaceIDFromPK(Long.parseLong(pk))));
		statement.execute();
		ResultSet resultSet = statement.getResultSet();
		resultSet.next();
		return resultSet.getString("LOGINNAME");
	}

	public static String getClusterIP(Connection connection, String clusterID) throws SQLException
	{
		PreparedStatement statement = connection.prepareStatement("SELECT GridAddress.ADDRESS FROM DBCluster INNER JOIN GridServices ON DBCluster.DBCLUSTERID=GridServices.SERVICEID INNER JOIN GridAccount ON GridServices.ACCOUNTID=GridAccount.ACCOUNTID INNER JOIN GridAddress ON GridAccount.ADDRESSID=GridAddress.ADDRESSID INNER JOIN GridVirtualNode ON GridServices.ACCOUNTID=GridVirtualNode.CLUSTERID INNER JOIN GridResources ON GridServices.SERVICEID=GridResources.RESOURCEID INNER JOIN DatabaseDrivers ON DBCluster.DBDRIVERID=DatabaseDrivers.DBDRIVERID INNER JOIN DBClusterConfiguration ON DBCluster.DBCLUSTERID=DBClusterConfiguration.CLUSTERID WHERE  (((DBCluster.DBCLUSTERID = ?) AND ((GridResources.ADMINSTATUS = 1) AND ((GridResources.OPERATIONALSTATUS = 1) ))) AND (((DBCluster.DBCLUSTERID >= 0) AND (DBCluster.DBCLUSTERID <= 999999999999)) OR ((DBCluster.DBCLUSTERID >= 0) AND (DBCluster.DBCLUSTERID <= 999999999999))))");
		statement.setInt(1, Integer.parseInt(clusterID));
		statement.execute();
		ResultSet resultSet = statement.getResultSet();
		resultSet.next();
		return resultSet.getString("ADDRESS");
	}

	public static Pair<String, String> getMasterSlaveIPPair(Connection connection, String clusterIP) throws SQLException
	{
		PreparedStatement statement = connection.prepareStatement("Select master.address as masterip,slave.address as slaveip from GridAddress as main inner join GridAccount on main.addressid = GridAccount.addressid and main.address = ? inner join GridVirtualNode on GridAccount.accountid = GridVirtualNode.clusterid inner join GridAddress as master on master.addressid = GridVirtualNode.masterid inner join GridAddress as slave on slave.addressid = GridVirtualNode.slaveid");
		statement.setObject(1, clusterIP);
		statement.execute();
		ResultSet resultSet = statement.getResultSet();
		resultSet.next();
		return new ImmutablePair<>(resultSet.getString("masterip"), resultSet.getString("slaveip"));
	}

	public static Connection getDBConnection(String server, String ip, String db, String user, String password) throws Exception
	{
		try
		{
			Connection conn;
			if(StringUtils.equals("mysql", server))
			{
				Class.forName("com.mysql.jdbc.Driver");
				conn = DriverManager.getConnection(MessageFormat.format("jdbc:mysql://{0}:3306/{1}?connectTimeout=5000&useSSL=false", ip, db), user, password);
			}
			else
			{
				Class.forName("org.postgresql.Driver");
				conn = DriverManager.getConnection(MessageFormat.format("jdbc:postgresql://{0}:5432/sasdb?currentSchema={1}&connectTimeout=5000&useSSL=false", ip, db), user, password);
			}
			conn.setAutoCommit(false);
			return conn;
		}
		catch(Exception e)
		{
			CONNECTION_RETRY_ATTEMPT.set(CONNECTION_RETRY_ATTEMPT.get() + 1);
			if(CONNECTION_RETRY_ATTEMPT.get() <= 3)
			{
				TimeUnit.SECONDS.sleep(2);
				return getDBConnection(server, ip, db, user, password);
			}
			throw e;
		}
		finally
		{
			CONNECTION_RETRY_ATTEMPT.set(0);
		}
	}

	public static void addUserDetails(String server, String ip, String db, String user, String password, Map<String, Object> resultMap, Long sasStartRange, Long sasEndRange)
	{
		if(getUserDetails("UserID", server, ip, db, user, password, resultMap, sasStartRange, sasEndRange))
		{
			return;
		}
		getUserDetails("ID", server, ip, db, user, password, resultMap, sasStartRange, sasEndRange);
	}

	private static boolean getUserDetails(String userPk, String server, String ip, String db, String user, String password, Map<String, Object> resultMap, Long sasStartRange, Long sasEndRange)
	{
		try(Connection connection = getDBConnection(server, ip, db, user, password))
		{
			PreparedStatement preparedStatement = connection.prepareStatement(MessageFormat.format("Select Users.Name, Users.Email, Users.ZUID from Users where {0}>= ? AND {0}<=?", userPk));
			preparedStatement.setObject(1, sasStartRange);
			preparedStatement.setObject(2, sasEndRange);

			preparedStatement.execute();
			ResultSet resultSet = preparedStatement.getResultSet();

			List<Map> userList = new ArrayList();
			while(resultSet.next())
			{
				Map<String, String> userDetails = new LinkedHashMap<>();

				userDetails.put("name", resultSet.getString("Name"));
				userDetails.put("email", resultSet.getString("Email"));
				userDetails.put("zuid", resultSet.getString("ZUID"));

				userList.add(userDetails);
			}
			resultMap.put("users", userList);
			return true;
		}
		catch(Exception e)
		{
			return false;
		}

	}

	public static void handleQuery(String query, String server, String ip, String db, String user, String password, Map<String, Object> resultMap, Long sasStartRange, Long sasEndRange, boolean skipScoping)
	{
		String pkName = null;
		if(query == null || query.replaceAll(" ", "").length() == 0)
		{
			return;
		}
		query = query.replaceAll("\n", " ");
		query = query.replaceAll("(\\.|;)$", "");
		try(Connection connection = getDBConnection(server, ip, db, user, password))
		{
			PreparedStatement preparedStatement;

			Pattern selectQuerPattern = Pattern.compile("(?i)(select)(.*)(from)\\s+(\\w+)(.*)");
			Matcher matcher = selectQuerPattern.matcher(query);
			Pattern updatePattern = Pattern.compile("(?i)(Update)\\s+(\\w+)\\s+(?i)(set)(.*)\\s+(?i)(where)\\s+(.*)");
			Matcher updateMatcher = updatePattern.matcher(query);
			if(matcher.matches())
			{
				ResultSet primaryKeys = connection.getMetaData().getPrimaryKeys(null, "jbossdb", matcher.group(4));
				while(primaryKeys.next())
				{
					pkName = primaryKeys.getString("COLUMN_NAME");
					if(pkName.matches("(.*)(?i)(id)"))
					{
						Pattern allisaPattern = Pattern.compile("(?i)(select)(.*)(from)\\s+(\\w+)\\s+AS\\s+(\\w+)\\s+(.*)");
						Matcher aliasMatcher = allisaPattern.matcher(query);
						pkName = (aliasMatcher.matches() ? aliasMatcher.group(5) : matcher.group(4)).concat(".").concat(pkName);
						break;
					}
				}
			}
			else if(updateMatcher.matches())
			{
				Map<String, String> response = ZohoAPI.doAuthentication();
				if(Objects.nonNull(response))
				{
					resultMap.putAll(response);
					return;
				}
				ResultSet primaryKeys = connection.getMetaData().getPrimaryKeys(null, "jbossdb", updateMatcher.group(2));
				while(primaryKeys.next())
				{
					pkName = primaryKeys.getString("COLUMN_NAME");
					if(pkName.matches("(.*)(?i)(id)"))
					{
						break;
					}
					else
					{
						pkName = null;
					}
				}

				int datType = -1;
				ResultSet resultSet = connection.getMetaData().getColumns(null, "jbossdb", updateMatcher.group(2), null);
				while(resultSet.next())
				{
					if(StringUtils.equals(pkName, resultSet.getString("COLUMN_NAME")))
					{
						datType = Integer.parseInt(resultSet.getString("DATA_TYPE"));
					}
				}
				if(StringUtils.isEmpty(pkName) || datType != Types.BIGINT)
				{
					throw new AppException("Update query cannot be performed as PK column with BIGINT type is not found for this table!");
				}
			}
			else
			{
				resultMap.put("query_output", "Invalid query");
				return;
			}

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

			if(skipScoping || pkName == null || !pkName.matches("(.*)(?i)(id)") || pkName.equalsIgnoreCase("zsid") || pkName.equalsIgnoreCase("zuid"))
			{
				preparedStatement = connection.prepareStatement(startQuery + endQuery);
			}
			else
			{
				if(query.matches("(.*)(?i)(where)(.*)"))
				{
					preparedStatement = connection.prepareStatement(startQuery + MessageFormat.format(" AND {0}>= ? AND {0}<=? ", pkName).concat(endQuery));
				}
				else
				{
					preparedStatement = connection.prepareStatement(startQuery + MessageFormat.format(" where {0}>= ? AND {0}<=? ", pkName).concat(endQuery));
				}
				preparedStatement.setObject(1, sasStartRange);
				preparedStatement.setObject(2, sasEndRange);
			}

			if(!query.matches("(?i)select(.*)"))
			{
				int updatedRecords = preparedStatement.executeUpdate();
				if(updatedRecords > 1)
				{
					connection.rollback();
					throw new AppException("Query cannot be executed as it affects more than one record");
				}

				connection.commit();
				resultMap.put("query_output", "Update query executed successfully");
				return;
			}

			preparedStatement.execute();
			List queryOutput = getQueryOutput(preparedStatement);
			resultMap.put("query_output", queryOutput);
		}
		catch(Exception e)
		{
			resultMap.put("query_output", e.getMessage());
		}

	}

	static List<Map<String, String>> getQueryOutput(PreparedStatement preparedStatement) throws SQLException
	{
		ResultSet resultSet = preparedStatement.getResultSet();
		ResultSetMetaData resultSetMetaData = resultSet.getMetaData();

		List<Map<String, String>> queryOutput = new ArrayList<>();
		while(resultSet.next())
		{
			Map<String, String> row = new LinkedHashMap<>();
			for(int i = 1; i <= resultSetMetaData.getColumnCount(); i++)
			{
				row.put(resultSetMetaData.getColumnLabel(i).toUpperCase(), resultSet.getString(i));
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
		return queryOutput;
	}

	static void handleDecryption(HttpServletRequest request, JSONObject credentials) throws Exception
	{
		try
		{
			PrivateKey privateKey = (PrivateKey) request.getSession().getAttribute("private_key");
			if(privateKey == null)
			{
				throw new Exception("key_expired");
			}
			credentials.put("ip", Util.decryptData(privateKey, credentials.getString("ip")));
			credentials.put("user", Util.decryptData(privateKey, credentials.getString("user")));
			credentials.put("password", Util.decryptData(privateKey, credentials.getString("password")));
		}
		catch(Exception e)
		{
			//String errorMessage = StringUtils.equals(e.getMessage(), "key_expired") ? "key_expired" : "Credentials tampered. Please refresh the page and try again.";
			throw new Exception("key_expired");
		}

	}
}
