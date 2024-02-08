package com.server.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.MessageFormat;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.dbcp.dbcp2.BasicDataSource;

import com.server.common.Configuration;

public class DBUtil
{
	private static DataSource dataSource;
	public static String schemaName;

	public static boolean isMysql;

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
}
