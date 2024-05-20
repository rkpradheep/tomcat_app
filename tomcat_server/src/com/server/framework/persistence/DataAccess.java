package com.server.framework.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

public class DataAccess
{
	public static class Transaction
	{
		private static final ThreadLocal<Connection> TRANSACTION_TL = new ThreadLocal<>();

		static Connection getActiveTxnFromTL() throws Exception
		{
			Connection connection = TRANSACTION_TL.get();
			if(Objects.nonNull(connection))
			{
				if(!connection.isClosed())
				{
					return connection;
				}

				TRANSACTION_TL.remove();
			}
			return null;
		}

		public static void begin() throws Exception
		{
			Connection connection = TRANSACTION_TL.get();
			if(Objects.nonNull(connection))
			{
				if(!connection.isClosed())
				{
					connection.rollback();
					connection.close();
				}
			}

			TRANSACTION_TL.set(DBUtil.getServerDBConnection());
		}

		public static void commit() throws Exception
		{
			Connection connection = TRANSACTION_TL.get();
			if(Objects.isNull(connection))
			{
				throw new Exception("No active transaction found to commit");
			}

			connection.commit();
			TRANSACTION_TL.remove();
		}

		public static void rollback()
		{
			try
			{
				Connection connection = TRANSACTION_TL.get();
				if(Objects.isNull(connection))
				{
					return;
				}

				connection.rollback();
				TRANSACTION_TL.remove();
			}
			catch(Exception e)
			{

			}
		}
	}

	public static void add(Row row) throws Exception
	{
		DataObject dataObject = new DataObject();
		dataObject.addRow(row);

		add(dataObject);
	}

	public static void add(DataObject dataObject) throws Exception
	{
		List<Row> rowList = dataObject.getRows();
		DataAccessUtil.populatePK(rowList);

		Connection connection = null;

		try
		{
			connection = DBUtil.getServerDBConnectionForTxn();

			for(Row row : rowList)
			{

				PreparedStatement preparedStatement = connection.prepareStatement(DataAccessUtil.getInsertQueryString(row), PreparedStatement.RETURN_GENERATED_KEYS);

				int i = 1;
				for(Object columnValue : row.getRowMap().values())
				{
					preparedStatement.setObject(i++, columnValue);
				}

				preparedStatement.executeUpdate();
			}

			if(Objects.isNull(Transaction.getActiveTxnFromTL()))
			{
				connection.commit();
			}
		}
		catch(Exception e)
		{
			DataAccessUtil.handleExceptionForTxn(connection);
		}
		finally
		{
			DataAccessUtil.handlePostProcessForTxn(connection);
		}
	}

	public static DataObject get(SelectQuery selectQuery) throws Exception
	{

		DataObject dataObject = new DataObject();

		Connection connection = null;

		try
		{
			connection = DBUtil.getServerDBConnectionForTxn();

			List<Object> placeHolderList = new ArrayList<>();

			PreparedStatement preparedStatement = connection.prepareStatement(DataAccessUtil.getSelectQueryString(selectQuery, placeHolderList));

			int i = 1;
			for(Object columnValue : placeHolderList)
			{
				preparedStatement.setObject(i++, columnValue);
			}

			ResultSet resultSet = preparedStatement.executeQuery();

			while(resultSet.next())
			{
				List<String> tableResultList = new ArrayList<>();
				tableResultList.add(selectQuery.tableName);
				for(SelectQuery.Join join : selectQuery.joinList)
				{
					if(!tableResultList.contains(join.baseTableName))
					{
						tableResultList.add(join.baseTableName);
					}
					if(!tableResultList.contains(join.referenceTableName))
					{
						tableResultList.add(join.referenceTableName);
					}
				}

				Row row = new Row(selectQuery.tableName);

				for(String tableName : tableResultList)
				{
					List<String> columnList = DBUtil.columnList(tableName);

					for(String columnName : columnList)
					{
						row.set(tableName, columnName, resultSet.getObject(tableName + "." + columnName));
					}
				}
				dataObject.addRow(row);
			}
			return dataObject;
		}
		finally
		{
			DataAccessUtil.handlePostProcessForTxn(connection);
		}

	}

	public static void update(UpdateQuery updateQuery) throws Exception
	{
		Connection connection = null;
		try
		{
			connection = DBUtil.getServerDBConnectionForTxn();

			List<Object> valuePlaceHolderList = new ArrayList<>();
			PreparedStatement preparedStatement = connection.prepareStatement(DataAccessUtil.getUpdateQueryString(updateQuery, valuePlaceHolderList));

			int i = 1;
			for(Object columnValue : valuePlaceHolderList)
			{
				preparedStatement.setObject(i++, columnValue);
			}

			preparedStatement.executeUpdate();
		}
		catch(Exception e)
		{
			DataAccessUtil.handleExceptionForTxn(connection);
		}
		finally
		{
			DataAccessUtil.handlePostProcessForTxn(connection);
		}
	}

	public static void delete(String tableName, Criteria criteria) throws Exception
	{
		Connection connection = null;
		try
		{
			connection = DBUtil.getServerDBConnectionForTxn();

			List<Object> valuePlaceHolderList = new ArrayList<>();
			PreparedStatement preparedStatement = connection.prepareStatement(DataAccessUtil.getDeleteQueryString(tableName, criteria, valuePlaceHolderList));

			int i = 1;
			for(Object columnValue : valuePlaceHolderList)
			{
				preparedStatement.setObject(i++, columnValue);
			}

			preparedStatement.execute();
		}
		catch(Exception e)
		{
			DataAccessUtil.handleExceptionForTxn(connection);
		}
		finally
		{
			DataAccessUtil.handlePostProcessForTxn(connection);
		}
	}
}
