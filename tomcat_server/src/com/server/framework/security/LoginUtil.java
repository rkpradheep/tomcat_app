package com.server.framework.security;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.codec.digest.DigestUtils;

import com.server.framework.common.DateUtil;
import com.server.framework.user.User;
import com.server.framework.user.UserUtil;

public class LoginUtil
{
	private static final Logger LOGGER = Logger.getLogger(LoginUtil.class.getName());

	public static User validateCredentials(String name, String password)
	{
		name = name.toUpperCase().trim();

		String selectQuery = "SELECT * FROM Users where name = ? AND password = ?";

		try(Connection connection = DBUtil.getServerDBConnection())
		{
			PreparedStatement preparedStatement = connection.prepareStatement(selectQuery);
			preparedStatement.setString(1, name.toUpperCase().trim());
			preparedStatement.setString(2, DigestUtils.sha256Hex(password.trim()));

			return UserUtil.getUser(preparedStatement.executeQuery());
		}
		catch(Exception e)
		{
			LOGGER.log(Level.SEVERE, "Exception occurred", e);
			return null;
		}
	}

	public static void addSession(String sessionId, Long userId, boolean isAdminLogin)
	{
		try(Connection connection = DBUtil.getServerDBConnection())
		{
			long expiryTime = isAdminLogin ? (1000 * 60 * 60 * 24) : (1000 * 60 * 30);

			String insertQuery = "INSERT INTO SessionManagement (id, user_id, expiry_time) VALUES (?,?,?)";

			PreparedStatement preparedStatement = connection.prepareStatement(insertQuery);
			preparedStatement.setString(1, sessionId);
			preparedStatement.setLong(2, userId);
			preparedStatement.setLong(3, DateUtil.getCurrentTimeInMillis() + expiryTime);

			preparedStatement.executeUpdate();
		}
		catch(Exception e)
		{
			LOGGER.log(Level.SEVERE, "Exception occurred", e);
		}
	}

	public static void deleteExpiredSessions() throws Exception
	{
		try(Connection connection = DBUtil.getServerDBConnection())
		{
			String deleteQuery = "DELETE FROM SessionManagement WHERE expiry_time < ?";

			PreparedStatement preparedStatement = connection.prepareStatement(deleteQuery);
			preparedStatement.setLong(1, DateUtil.getCurrentTimeInMillis());

			preparedStatement.executeUpdate();
		}
		catch(Exception e)
		{
			LOGGER.log(Level.SEVERE, "Exception occurred", e);
		}
	}
}
