package com.server.security;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;

import com.server.db.DBUtil;

public class LoginUtil
{
	private static final Logger LOGGER = Logger.getLogger(LoginUtil.class.getName());

	public static Long validateCredentials(String name, String password, boolean isAdmin)
	{
		name = name.toUpperCase().trim();

		String selectQuery = "SELECT * FROM Users where name = ? AND password = ? AND role_type = ?";

		try(Connection connection = DBUtil.getServerDBConnection())
		{
			PreparedStatement preparedStatement = connection.prepareStatement(selectQuery);
			preparedStatement.setString(1, name.toUpperCase().trim());
			preparedStatement.setString(2, password.trim());
			preparedStatement.setInt(3, isAdmin? -1 : 0);

			ResultSet resultSet = preparedStatement.executeQuery();
			return resultSet.next() ? resultSet.getLong("id") : null;
		}
		catch(Exception e)
		{
			LOGGER.log(Level.SEVERE, "Exception occurred", e);
			return null;
		}
	}

	public static User getUser(String sessionId)
	{
		if(StringUtils.isEmpty(sessionId))
		{
			return null;
		}

		String selectQuery = "SELECT * FROM SessionManagement INNER JOIN Users on SessionManagement.user_id = Users.id where SessionManagement.id = ?";

		try(Connection connection = DBUtil.getServerDBConnection())
		{
			PreparedStatement preparedStatement = connection.prepareStatement(selectQuery);
			preparedStatement.setString(1, sessionId);

			ResultSet resultSet = preparedStatement.executeQuery();
			if(resultSet.next())
			{
				return new User(resultSet.getLong("user_id"), resultSet.getString("name"), sessionId, resultSet.getLong("expiry_time"));
			}
			return null;
		}
		catch(Exception e)
		{
			LOGGER.log(Level.SEVERE, "Exception occurred", e);
			return null;
		}
	}

	public static boolean addSession(String sessionId, Long userId)
	{
		try(Connection connection = DBUtil.getServerDBConnection())
		{
			String insertQuery = "INSERT INTO SessionManagement (id, user_id, expiry_time) VALUES (?,?,?)";

			PreparedStatement preparedStatement = connection.prepareStatement(insertQuery);
			preparedStatement.setString(1, sessionId);
			preparedStatement.setLong(2, userId);
			preparedStatement.setLong(3, System.currentTimeMillis() + (1000 * 60 * 30));

			preparedStatement.executeUpdate();
			return true;
		}
		catch(Exception e)
		{
			LOGGER.log(Level.SEVERE, "Exception occurred", e);
			return false;
		}
	}

	public static void deleteExpiredSessions() throws Exception
	{
		try(Connection connection = DBUtil.getServerDBConnection())
		{
			String deleteQuery = "DELETE FROM SessionManagement WHERE expiry_time < ?";

			PreparedStatement preparedStatement = connection.prepareStatement(deleteQuery);
			preparedStatement.setLong(1, System.currentTimeMillis());

			preparedStatement.executeUpdate();
		}
	}
}