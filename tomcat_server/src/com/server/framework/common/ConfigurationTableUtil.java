package com.server.framework.common;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.server.framework.security.DBUtil;

public class ConfigurationTableUtil
{
	private static final Logger LOGGER = Logger.getLogger(ConfigurationTableUtil.class.getName());

	public static String getValue(String key)
	{
		String jobSelectQuery = "SELECT * FROM Configuration where ckey = ?";
		try(Connection connection = DBUtil.getServerDBConnection())
		{
			PreparedStatement preparedStatement = connection.prepareStatement(jobSelectQuery);
			preparedStatement.setString(1, key);

			ResultSet resultSet = preparedStatement.executeQuery();
			resultSet.next();

			return resultSet.getString("cvalue");
		}
		catch(Exception e)
		{
			LOGGER.log(Level.SEVERE, "Exception occurred",e );
			return null;
		}
	}

	public static void setValue(String key, String value)
	{
		try(Connection connection = DBUtil.getServerDBConnection())
		{
			PreparedStatement preparedStatement;
			String updateJobQuery = "Insert into Configuration values (?, ?)";
			preparedStatement = connection.prepareStatement(updateJobQuery);
			preparedStatement.setString(1, key);
			preparedStatement.setString(2, value);

			preparedStatement.execute();
		}
		catch(Exception e)
		{
			LOGGER.log(Level.SEVERE, "Exception occurred",e );
		}
	}

	public static void delete(String key)
	{
		try(Connection connection = DBUtil.getServerDBConnection())
		{
			PreparedStatement preparedStatement;
			String updateJobQuery = "Delete from Configuration where ckey= (?)";
			preparedStatement = connection.prepareStatement(updateJobQuery);
			preparedStatement.setString(1, key);
			preparedStatement.execute();
		}
		catch(Exception e)
		{
			LOGGER.log(Level.SEVERE, "Exception occurred",e );
		}
	}
}
