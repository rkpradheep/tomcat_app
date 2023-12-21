package com.server.chat;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.server.db.DBUtil;

public class ChatWebSocketUtil
{
	private static final Logger LOGGER = Logger.getLogger(ChatWebSocketUtil.class.getName());

	static long addOrGetUser(String name)
	{
		name = name.toUpperCase().trim();

		String selectQuery = "SELECT * FROM ChatUser where name = ?";

		try(Connection connection = DBUtil.getServerDBConnection())
		{
			PreparedStatement preparedStatement = connection.prepareStatement(selectQuery);
			preparedStatement.setString(1, name);

			ResultSet resultSet = preparedStatement.executeQuery();
			if(resultSet.next())
			{
				return resultSet.getLong(1);
			}
			String insertQuery = "INSERT INTO ChatUser (name) VALUES (?)";

			preparedStatement = connection.prepareStatement(insertQuery, PreparedStatement.RETURN_GENERATED_KEYS);
			preparedStatement.setString(1, name);

			preparedStatement.executeUpdate();
			resultSet = preparedStatement.getGeneratedKeys();
			resultSet.next();

			return resultSet.getLong(1);
		}
		catch(Exception e)
		{
			LOGGER.log(Level.SEVERE, "Exception occurred", e);
			return 0L;
		}
	}

	static String getPreviousMessage(String name)
	{
		String selectQuery = "SELECT message FROM ChatUser INNER JOIN ChatUserDetails ON ChatUser.id = ChatUserDetails.chatuserid where ChatUser.name = '" + name + "'";

		try(Connection connection = DBUtil.getServerDBConnection())
		{
			PreparedStatement preparedStatement = connection.prepareStatement(selectQuery);

			ResultSet resultSet = preparedStatement.executeQuery();

			StringBuilder previousMessage = new StringBuilder();
			while(resultSet.next())
			{
				previousMessage.append(resultSet.getString("message"));
			}
			return previousMessage.toString();
		}
		catch(Exception e)
		{
			LOGGER.log(Level.SEVERE, "Exception occurred", e);
			return "";
		}
	}

	static void addMessage(String name, String message)
	{
		try(Connection connection = DBUtil.getServerDBConnection())
		{
			String insertQuery = "INSERT INTO ChatUserDetails (chatuserid, message) VALUES (?,?)";

			PreparedStatement preparedStatement = connection.prepareStatement(insertQuery, PreparedStatement.RETURN_GENERATED_KEYS);
			preparedStatement.setString(2, message);
			preparedStatement.setLong(1, addOrGetUser(name));

			preparedStatement.executeUpdate();
		}
		catch(Exception e)
		{
			LOGGER.log(Level.SEVERE, "Exception occurred", e);
		}
	}

	//	static void uploadFile(String name, InputStream inputStream)
	//	{
	//		try(Connection connection = DBUtil.getServerDBConnection())
	//		{
	//			String insertQuery = "INSERT INTO Uploads (name, file) VALUES (?,?)";
	//
	//			PreparedStatement preparedStatement = connection.prepareStatement(insertQuery, PreparedStatement.RETURN_GENERATED_KEYS);
	//			preparedStatement.setString(1, name);
	//			preparedStatement.setBinaryStream(2, inputStream);
	//
	//			preparedStatement.executeUpdate();
	//		}
	//		catch(Exception e)
	//		{
	//			LOGGER.log(Level.SEVERE, "Exception occurred", e);
	//		}
	//	}
}
