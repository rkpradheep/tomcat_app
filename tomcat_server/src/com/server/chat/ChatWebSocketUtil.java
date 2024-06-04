package com.server.chat;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.server.framework.persistence.Column;
import com.server.framework.persistence.Criteria;
import com.server.framework.persistence.DataAccess;
import com.server.framework.persistence.DataObject;
import com.server.framework.persistence.Function;
import com.server.framework.persistence.Join;
import com.server.framework.persistence.Row;
import com.server.framework.persistence.SelectQuery;
import com.server.table.constants.CHATUSER;
import com.server.table.constants.CHATUSERDETAIL;

public class ChatWebSocketUtil
{
	private static final Logger LOGGER = Logger.getLogger(ChatWebSocketUtil.class.getName());

	static long addOrGetUser(String name)
	{
		try
		{
			SelectQuery selectQuery = new SelectQuery(CHATUSER.TABLE);
			Function function = Function.createFunction(Function.Constants.UPPER, Column.getColumn(CHATUSER.TABLE, CHATUSER.NAME));
			selectQuery.setCriteria(new Criteria(function, name.toUpperCase().trim(), Criteria.Constants.EQUAL));

			DataObject dataObject = DataAccess.get(selectQuery);

			if(!dataObject.getRows().isEmpty())
			{
				return (long) dataObject.getRows().get(0).get(CHATUSER.ID);
			}

			Row row = new Row(CHATUSER.TABLE);
			row.set(CHATUSER.NAME, name);

			dataObject.addRow(row);

			DataAccess.add(dataObject);

			return (long) dataObject.getRows().get(0).get(CHATUSER.ID);
		}
		catch(Exception e)
		{
			LOGGER.log(Level.SEVERE, "Exception occurred", e);
			return 0L;
		}
	}

	static String getPreviousMessage(String name)
	{
		try
		{

			SelectQuery selectQuery = new SelectQuery(CHATUSER.TABLE);
			selectQuery.setCriteria(new Criteria(CHATUSER.TABLE, CHATUSER.NAME, name, Criteria.Constants.EQUAL));
			Join join = new Join(CHATUSER.TABLE, CHATUSER.ID, CHATUSERDETAIL.TABLE, CHATUSERDETAIL.CHATUSERID);
			selectQuery.addJoin(join, Join.Constants.INNER_JOIN);

			DataObject dataObject = DataAccess.get(selectQuery);

			StringBuilder previousMessage = new StringBuilder();
			for(Row row : dataObject.getRows())
			{
				previousMessage.append(row.get(CHATUSERDETAIL.TABLE, CHATUSERDETAIL.MESSAGE));
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
		try
		{
			Row row = new Row(CHATUSERDETAIL.TABLE);
			row.set(CHATUSERDETAIL.CHATUSERID, addOrGetUser(name));
			row.set(CHATUSERDETAIL.MESSAGE, message);

			DataAccess.add(row);
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
