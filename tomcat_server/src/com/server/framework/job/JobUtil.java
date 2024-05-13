package com.server.framework.job;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.server.framework.common.Configuration;
import com.server.framework.common.DateUtil;
import com.server.framework.security.DBUtil;

public class JobUtil
{
	private static final Logger LOGGER = Logger.getLogger(JobUtil.class.getName());

	public static Long scheduleJob(String taskName, String data, long milliSeconds) throws Exception
	{
		return scheduleJob(taskName, data, milliSeconds, -1, false);
	}

	public static Long scheduleJob(String taskName, String data, long milliSeconds, int dayInterval, boolean isRecurring) throws Exception
	{
		String insertQuery = "INSERT INTO Job (task_name, data, scheduled_time, day_interval, is_recurring) VALUES (?, ?, ?, ?, ?)";
		long id;
		try(Connection connection = DBUtil.getServerDBConnection())
		{

			PreparedStatement preparedStatement = connection.prepareStatement(insertQuery, PreparedStatement.RETURN_GENERATED_KEYS);
			preparedStatement.setString(1, taskName);
			preparedStatement.setString(2, data);
			preparedStatement.setLong(3, DateUtil.getCurrentTimeInMillis() + milliSeconds);
			preparedStatement.setInt(4, dayInterval);
			preparedStatement.setBoolean(5, isRecurring);

			preparedStatement.executeUpdate();
			ResultSet resultSet = preparedStatement.getGeneratedKeys();
			resultSet.next();

			id = resultSet.getLong(1);
		}

		if(!Configuration.getBoolean("can.add.job.dispatcher"))
		{
			RefreshManager.addJobInQueue(id, TaskEnum.getHandler(taskName), data, milliSeconds);
		}

		LOGGER.log(Level.INFO, "Job added with ID {0} for task {1} with delay {2} seconds at {3}", new Object[] {Long.toString(id), taskName, milliSeconds / 1000, DateUtil.getFormattedCurrentTime()});
		return id;
	}

	public static void scheduleJob(CustomRunnable runnable, int seconds)
	{
		RefreshManager.addJobInQueue(runnable, seconds);
	}

	static void handlePostProcess(long id)
	{
		String jobSelectQuery = "SELECT * FROM Job where id = ?";
		try(Connection connection = DBUtil.getServerDBConnection())
		{
			PreparedStatement preparedStatement = connection.prepareStatement(jobSelectQuery);
			preparedStatement.setLong(1, id);

			ResultSet resultSet = preparedStatement.executeQuery();
			resultSet.next();
			if(!resultSet.getBoolean("is_recurring"))
			{
				deleteJob(id);
				return;
			}

			String task = resultSet.getString("task_name");
			String data = resultSet.getString("data");
			long scheduledTime = resultSet.getLong("scheduled_time");
			scheduledTime = JobUtil.getNextExecutionTimeFromPreviousScheduleTime(scheduledTime, resultSet.getInt("day_interval"));

			String updateJobQuery = "Update Job set scheduled_time=? where id=?";
			preparedStatement = connection.prepareStatement(updateJobQuery);
			preparedStatement.setLong(1, scheduledTime);
			preparedStatement.setLong(2, id);

			preparedStatement.executeUpdate();

			RefreshManager.addJobInQueue(id, TaskEnum.getHandler(task), data, scheduledTime - DateUtil.getCurrentTimeInMillis());
		}
		catch(Exception e)
		{
			LOGGER.log(Level.SEVERE, "Exception occurred do post process", e);
		}
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

	static long getNextExecutionDelayTimeFromStartDate(String startDate, String startDateFormat, long dayInterval, String timeOfDay)
	{
		int hour = Integer.parseInt(timeOfDay.split(":")[0]);
		int minute = Integer.parseInt(timeOfDay.split(":")[1]);

		long milliseconds = DateUtil.getDayStartInMillis(startDate, startDateFormat);
		milliseconds = milliseconds + (DateUtil.ONE_DAY_IN_MILLISECOND * dayInterval);
		long nextExecutionTime = milliseconds + DateUtil.getDelayFromDayStart(hour, minute);

		return nextExecutionTime - DateUtil.getCurrentTimeInMillis();

	}

	static long getNextExecutionTimeFromPreviousScheduleTime(long previousExecution, int dayInterval)
	{
		long nextExecutionTime = previousExecution + (DateUtil.ONE_DAY_IN_MILLISECOND * dayInterval);

		while(nextExecutionTime < DateUtil.getCurrentTimeInMillis())
		{
			nextExecutionTime += (DateUtil.ONE_DAY_IN_MILLISECOND * dayInterval);
		}

		return nextExecutionTime;
	}
}
