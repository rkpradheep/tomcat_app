package com.server.framework.persistence;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.text.MessageFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.dbcp.dbcp2.BasicDataSource;

import com.server.framework.common.AppException;
import com.server.framework.common.Configuration;

public class DBUtil
{
	private static BasicDataSource dataSource;
	public static String schemaName;

	public static boolean isMysql;

	private static final Logger LOGGER = Logger.getLogger(DBUtil.class.getName());

	private static Map<String, List<String>> tablePkList = new HashMap<>();
	private static Map<String, List<String>> tableFkList = new HashMap<>();

	public static void initialiseDataSource()
	{
		try
		{
			String server = Configuration.getProperty("db.server");
			isMysql = StringUtils.equals("mysql", server);

			schemaName = Configuration.getProperty("db.server.schema");
			BasicDataSource basicDataSource = new BasicDataSource();

			basicDataSource.setDriverClassName(isMysql ? "com.mysql.jdbc.Driver" : "org.mariadb.jdbc.Driver");
			basicDataSource.setUrl(MessageFormat.format("jdbc:{0}://{1}:{2}/{3}?connectTimeout=5000&useSSL=false&allowPublicKeyRetrieval=True", server, Configuration.getProperty("db.server.ip"), Configuration.getProperty("db.server.port"), DBUtil.schemaName));
			basicDataSource.setUsername(Configuration.getProperty("db.server.user"));
			basicDataSource.setPassword(Configuration.getProperty("db.server.password"));
			basicDataSource.setMaxIdle(5);
			basicDataSource.setMinIdle(2);
			basicDataSource.setMaxTotal(150);
			basicDataSource.setMaxWait(Duration.ofSeconds(5));
			basicDataSource.setDefaultAutoCommit(false);
			basicDataSource.setAutoCommitOnReturn(false);

			basicDataSource.start();

			dataSource = basicDataSource;

			LOGGER.info("DataSource initialised successfully");
		}
		catch(Exception e)
		{
			LOGGER.log(Level.SEVERE, "Exception occurred while initialising data source", e);
		}
	}

	public static List<String> columnList(String tableName) throws Exception
	{
		List<String> columnList = new ArrayList<>();
		try(Connection connection = DBUtil.getServerDBConnection())
		{
			DatabaseMetaData databaseMetaData = connection.getMetaData();

			ResultSet columnResultSet = databaseMetaData.getColumns(null, DBUtil.schemaName, tableName, null);
			while(columnResultSet.next())
			{
				String columnName = columnResultSet.getString("COLUMN_NAME");
				columnList.add(columnName);
			}
		}
		return columnList;
	}

	public static List<String> getPKList(String tableName) throws Exception
	{
		if(tablePkList.containsKey(tableName))
		{
			return tablePkList.get(tableName);
		}
		List<String> pkList = new ArrayList<>();
		try(Connection connection = DBUtil.getServerDBConnection())
		{
			ResultSet resultSet = connection.getMetaData().getPrimaryKeys(null, schemaName, tableName);
			while(resultSet.next())
			{
				pkList.add(resultSet.getString("COLUMN_NAME"));
			}
			tablePkList.put(tableName, pkList);
		}
		return pkList;
	}

	public static List<String> getFKList(String tableName)
	{
		if(tableFkList.containsKey(tableName))
		{
			return tableFkList.get(tableName);
		}

		List<String> fkList = new ArrayList<>();
		try(Connection connection = DBUtil.getServerDBConnection())
		{
			ResultSet resultSet = connection.getMetaData().getImportedKeys(null, schemaName, tableName);

			while(resultSet.next())
			{
				fkList.add(resultSet.getString("FKCOLUMN_NAME"));
			}
			tableFkList.put(tableName, fkList);
		}
		catch(Exception e)
		{
		}
		return fkList;
	}

	public static Connection getServerDBConnection() throws Exception
	{
		Connection connection = dataSource.getConnection();
		connection.setAutoCommit(true);
		return connection;
	}

	public static Connection getServerDBConnectionForTxn() throws Exception
	{
		return Objects.isNull(DataAccess.Transaction.getActiveTxnFromTL()) ? getDataSource().getConnection() : DataAccess.Transaction.getActiveTxnFromTL();
	}

	public static void closeDataSource()
	{
		try
		{
			if(!getDataSource().isClosed())
			{
				getDataSource().close();
				LOGGER.info("Datasource closed successfully");
			}
		}
		catch(Exception e)
		{
			LOGGER.log(Level.SEVERE, "Exception occurred while closing data source", e);
		}
	}

	private static BasicDataSource getDataSource() throws Exception
	{
		if(Objects.isNull(dataSource))
		{
			initialiseDataSource();
		}
		if(Objects.isNull(dataSource))
		{
			throw new AppException("Database connection failed");
		}
		return dataSource;
	}
}
