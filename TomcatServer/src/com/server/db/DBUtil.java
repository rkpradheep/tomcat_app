package com.server.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.MessageFormat;

import javax.sql.DataSource;

import org.apache.tomcat.dbcp.dbcp2.BasicDataSource;

import com.server.common.Configuration;

public class DBUtil
{
	private static DataSource dataSource;

	public static void initialiseDataSource()
	{
		BasicDataSource basicDataSource = new BasicDataSource();

		basicDataSource.setDriverClassName("com.mysql.jdbc.Driver");
		basicDataSource.setUrl(MessageFormat.format("jdbc:mysql://{0}:{1}/{2}?connectTimeout=5000", Configuration.getProperty("db.server.ip"), Configuration.getProperty("db.server.port"), "serverdb"));
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
