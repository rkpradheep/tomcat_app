package com.server.security;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.dbcp.dbcp2.BasicDataSource;

public class DBUtil
{
	private static BasicDataSource dataSource;
	public static String schemaName;

	public static boolean isMysql;

	private static final Logger LOGGER = Logger.getLogger(DBUtil.class.getName());

	public static void initialiseDataSource()
	{
		String server =  Configuration.getProperty("db.server");
		isMysql = StringUtils.equals("mysql", server);

		schemaName = Configuration.getProperty("db.server.schema");
		BasicDataSource basicDataSource = new BasicDataSource();

		basicDataSource.setDriverClassName(isMysql? "com.mysql.jdbc.Driver" : "org.mariadb.jdbc.Driver");
		basicDataSource.setUrl(MessageFormat.format("jdbc:{0}://{1}:{2}/{3}?connectTimeout=5000&useSSL=false&allowPublicKeyRetrieval=True", server, Configuration.getProperty("db.server.ip"), Configuration.getProperty("db.server.port"), DBUtil.schemaName));
		basicDataSource.setUsername(Configuration.getProperty("db.server.user"));
		basicDataSource.setPassword(Configuration.getProperty("db.server.password"));
		basicDataSource.setMaxIdle(10);
		basicDataSource.setMinIdle(5);
		basicDataSource.setMaxTotal(20);

		dataSource = basicDataSource;
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
