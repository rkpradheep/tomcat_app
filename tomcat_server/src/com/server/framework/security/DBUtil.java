package com.server.framework.security;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.dbcp.dbcp2.BasicDataSource;

import com.server.framework.common.Configuration;

public class DBUtil
{
	private static BasicDataSource dataSource;
	public static String schemaName;

	public static boolean isMysql;

	private static final Logger LOGGER = Logger.getLogger(DBUtil.class.getName());

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
			basicDataSource.setMaxTotal(20);
			basicDataSource.start();

			dataSource = basicDataSource;

			LOGGER.info("DataSource initialised successfully");
		}
		catch(Exception e)
		{
			LOGGER.log(Level.SEVERE, "Exception occurred while initialising data source", e);
			throw new RuntimeException("DataSource initialisation failed");
		}
	}

	public static Connection getServerDBConnection() throws SQLException
	{
		return dataSource.getConnection();
	}

	public static void closeDataSource()
	{
		try
		{
			if(!dataSource.isClosed())
			{
				dataSource.close();
				LOGGER.info("Datasource closed successfully");
			}
		}
		catch(Exception e)
		{
			LOGGER.log(Level.SEVERE, "Exception occurred while closing data source", e);
		}
	}
}