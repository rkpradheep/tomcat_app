package com.server.framework.user;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import com.server.framework.common.DateUtil;
import com.server.framework.persistence.DBUtil;
import com.server.framework.persistence.DataAccess;
import com.server.framework.persistence.DataObject;
import com.server.framework.persistence.Row;

public class UserUtil
{
	private static final Logger LOGGER = Logger.getLogger(UserUtil.class.getName());

	public static User getUser(String sessionId, String authToken)
	{
		if(StringUtils.isBlank(sessionId) && StringUtils.isBlank(authToken))
		{
			return null;
		}

		String selectQuery = "SELECT * FROM Users INNER JOIN SessionManagement on  Users.id=SessionManagement.user_id where SessionManagement.id = ? AND expiry_time > ?";
		selectQuery = StringUtils.isBlank(authToken) ? selectQuery : "SELECT * FROM Users INNER JOIN AuthToken on  Users.id=AuthToken.user_id where AuthToken.token = ?";
		;

		try(Connection connection = DBUtil.getServerDBConnection())
		{
			PreparedStatement preparedStatement = connection.prepareStatement(selectQuery);
			preparedStatement.setString(1, StringUtils.defaultIfBlank(sessionId, authToken));
			if(StringUtils.isNotEmpty(sessionId))
			{
				preparedStatement.setLong(2, DateUtil.getCurrentTimeInMillis());
			}

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

		DataObject dataObject = new DataObject();
		Row row = new Row("Users");

		row.set("name", userJSON.getString("name"));
		row.set("password", DigestUtils.sha256Hex(userJSON.getString("password").trim()));
		row.set("role_type", RoleEnum.getType(userJSON.getString("role")));

		dataObject.addRow(row);
		DataAccess.add(dataObject);
	}
}
