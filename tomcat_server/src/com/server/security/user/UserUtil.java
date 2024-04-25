package com.server.security.user;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.transform.Result;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import com.server.security.DBUtil;

public class UserUtil
{
	private static final Logger LOGGER = Logger.getLogger(UserUtil.class.getName());
	public static User getUser(String sessionId, String authToken)
	{
		if(StringUtils.isBlank(sessionId) && StringUtils.isBlank(authToken))
		{
			return null;
		}

		String selectQuery = "SELECT * FROM Users INNER JOIN SessionManagement on  Users.id=SessionManagement.user_id where SessionManagement.id = ?";
		selectQuery = StringUtils.isBlank(authToken) ? selectQuery :  "SELECT * FROM Users INNER JOIN AuthToken on  Users.id=AuthToken.user_id where AuthToken.token = ?";;

		try(Connection connection = DBUtil.getServerDBConnection())
		{
			PreparedStatement preparedStatement = connection.prepareStatement(selectQuery);
			preparedStatement.setString(1, StringUtils.defaultIfBlank(sessionId, authToken));

			return getUser(preparedStatement.executeQuery());
		}
		catch(Exception e)
		{
			LOGGER.log(Level.SEVERE, "Exception occurred", e);
			return null;
		}
	}

	public static User getUser(ResultSet resultSet) throws Exception
	{
		return resultSet.next() ? new User(resultSet.getLong("id"), resultSet.getString("name"), resultSet.getInt("role_type") == RoleEnum.ADMIN.getType()) : null;
	}

	public static void addUser(JSONObject userJSON) throws Exception
	{

		String selectQuery = "Insert into Users (name, password, role_type) values (?, ?, ?)";

		try(Connection connection = DBUtil.getServerDBConnection())
		{
			PreparedStatement preparedStatement = connection.prepareStatement(selectQuery);
			preparedStatement.setString(1, userJSON.getString("name"));
			preparedStatement.setString(2, DigestUtils.sha256Hex(userJSON.getString("password").trim()));
			preparedStatement.setInt(3, RoleEnum.getType(userJSON.getString("role")));

			preparedStatement.executeUpdate();
		}
	}
}
