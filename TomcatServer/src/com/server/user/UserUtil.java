package com.server.user;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import com.server.db.DBUtil;

public class UserUtil
{
	private static final Logger LOGGER = Logger.getLogger(UserUtil.class.getName());
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

	public static void addUser(JSONObject userJSON) throws Exception
	{

		String selectQuery = "Insert into Users (name, password, role_type) values (?, ?, ?)";

		try(Connection connection = DBUtil.getServerDBConnection())
		{
			PreparedStatement preparedStatement = connection.prepareStatement(selectQuery);
			preparedStatement.setString(1, userJSON.getString("name"));
			preparedStatement.setString(2, userJSON.getString("password"));
			preparedStatement.setInt(3, RoleEnum.getType(userJSON.getString("role")));

			preparedStatement.executeUpdate();
		}
	}
}
