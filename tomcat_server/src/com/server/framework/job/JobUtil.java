package com.server.framework.job;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.server.framework.common.Configuration;
import com.server.framework.security.DBUtil;
import com.server.framework.security.SecurityUtil;

public class JobUtil
{
	private static final Logger LOGGER = Logger.getLogger(JobUtil.class.getName());

	public static Long scheduleJob(String taskName, String data, long milliSeconds) throws Exception
	{
		String insertQuery = "INSERT INTO Job (task_name, data, scheduled_time) VALUES (?, ?, ?)";
		long id;
		try(Connection connection = DBUtil.getServerDBConnection())
		{

			PreparedStatement preparedStatement = connection.prepareStatement(insertQuery, PreparedStatement.RETURN_GENERATED_KEYS);
			preparedStatement.setString(1, taskName);
			preparedStatement.setString(2, data);
			preparedStatement.setLong(3, System.currentTimeMillis() + milliSeconds);

			preparedStatement.executeUpdate();
			ResultSet resultSet = preparedStatement.getGeneratedKeys();
			resultSet.next();

			id = resultSet.getLong(1);
		}

		if(!Configuration.getBoolean("can.add.job.dispatcher"))
		{
			RefreshManager.addJobInQueue(id, TaskEnum.getHandler(taskName), data, milliSeconds);
		}

		LOGGER.log(Level.INFO, "Job added with ID {0} for task {1} with delay {2} seconds at {3}", new Object[] {Long.toString(id), taskName, milliSeconds / 1000, SecurityUtil.getFormattedCurrentTime()});
		return id;
	}

	public static void scheduleJob(CustomRunnable runnable, int seconds)
	{
		RefreshManager.addJobInQueue(runnable, seconds);
	}

	static void deleteJob(long id)
	{
		if(id == -1)
		{
			return;
		}

		try(Connection connection = DBUtil.getServerDBConnection())
		{
			String deleteQuery = "DELETE FROM Job WHERE id = ?";

			PreparedStatement preparedStatement = connection.prepareStatement(deleteQuery);
			preparedStatement.setLong(1, id);

			preparedStatement.executeUpdate();

			LOGGER.log(Level.INFO, "Job with ID {0} is deleted", new Object[] {id});
		}
		catch(Exception e)
		{
			LOGGER.log(Level.SEVERE, "Exception occurred while delete job", e);
		}
	}
}
