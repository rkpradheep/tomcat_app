package com.server.framework.job;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.codec.digest.DigestUtils;

import com.server.framework.common.Configuration;
import com.server.framework.common.ConfigurationTableUtil;
import com.server.framework.common.DateUtil;
import com.server.framework.security.DBUtil;

public class JobUtil
{
	private static final Logger LOGGER = Logger.getLogger(JobUtil.class.getName());
	public static final ThreadLocal<JobMeta> SCHEDULER_TL = new ThreadLocal<>();

	public static Long scheduleJob(String taskName, String data, long milliSeconds) throws Exception
	{
		return scheduleJob(taskName, data, milliSeconds, -1, false);
	}

	public static Long scheduleJob(String taskName, String data, long milliSeconds, int dayInterval, boolean isRecurring) throws Exception
	{
		String insertQuery = "INSERT INTO Job (task_name, data, scheduled_time, day_interval, is_recurring) VALUES (?, ?, ?, ?, ?)";
		long id;
		long executionTime = DateUtil.getCurrentTimeInMillis() + milliSeconds;
		try(Connection connection = DBUtil.getServerDBConnection())
		{

			PreparedStatement preparedStatement = connection.prepareStatement(insertQuery, PreparedStatement.RETURN_GENERATED_KEYS);
			preparedStatement.setString(1, taskName);
			preparedStatement.setString(2, data);
			preparedStatement.setLong(3, executionTime);
			preparedStatement.setInt(4, dayInterval);
			preparedStatement.setBoolean(5, isRecurring);

			preparedStatement.executeUpdate();
			ResultSet resultSet = preparedStatement.getGeneratedKeys();
			resultSet.next();

			id = resultSet.getLong(1);
			if(isRecurring)
			{
				ConfigurationTableUtil.setValue(DigestUtils.sha1Hex(String.valueOf(id)), String.valueOf(id));
			}
		}

		if(!Configuration.getBoolean("can.add.job.dispatcher"))
		{
			RefreshManager.addJobInQueue(id);
		}

		LOGGER.log(Level.INFO, "Job added with ID {0} for task {1} with delay {2} seconds. Formatted execution time : {3}", new Object[] {Long.toString(id), taskName, milliSeconds / 1000, DateUtil.getFormattedTime(executionTime)});
		return id;
	}

	public static void scheduleJob(CustomRunnable runnable, int seconds)
	{
		scheduleJob(runnable, seconds * 1000L);
	}
	public static void scheduleJob(CustomRunnable runnable, long millisecond)
	{
		scheduleJob(runnable, null, millisecond);
	}

	public static void scheduleJob(CustomRunnable runnable, String data, long millisecond)
	{
		RefreshManager.addJobInQueue(runnable, data, millisecond);
	}

	public static JobMeta getJobMeta(long jobId)
	{
		if(jobId == -1)
			return null;

		String jobSelectQuery = "SELECT * FROM Job where id = ?";
		try(Connection connection = DBUtil.getServerDBConnection())
		{
			PreparedStatement preparedStatement = connection.prepareStatement(jobSelectQuery);
			preparedStatement.setLong(1, jobId);

			ResultSet resultSet = preparedStatement.executeQuery();
			resultSet.next();

			String task = resultSet.getString("task_name");
			String data = resultSet.getString("data");
			long scheduledTime = resultSet.getLong("scheduled_time");
			boolean isRecurring = resultSet.getBoolean("is_recurring");
			int dayInterval = resultSet.getInt("day_interval");

			return new JobMeta.Builder()
				.setId(jobId)
				.setData(data)
				.setTaskName(task)
				.setRecurring(isRecurring)
				.setScheduledTime(scheduledTime)
				.setIntervalInDays(dayInterval)
				.setRunnable(TaskEnum.getRunnable(task))
				.build();
		}
		catch(Exception e)
		{
			LOGGER.log(Level.SEVERE, "Exception occurred while fetch job", e);
			return null;
		}

	}

	static void handlePostProcess(JobMeta jobMeta)
	{
		if(jobMeta.getId() == -1)
		{
			return;
		}
		if(!jobMeta.isRecurring())
		{
			deleteJob(jobMeta.getId());
			return;
		}

		updateNextExecutionTime(jobMeta.getId(), JobUtil.getNextExecutionTimeFromPreviousScheduleTime(jobMeta.getScheduledTime(), jobMeta.getIntervalInDays()));

		RefreshManager.addJobInQueue(jobMeta.getId());
	}

	static void updateNextExecutionTime(long jobId, long nextExecutionTime)
	{
		try(Connection connection = DBUtil.getServerDBConnection())
		{
			PreparedStatement preparedStatement;
			String updateJobQuery = "Update Job set scheduled_time=? where id=?";
			preparedStatement = connection.prepareStatement(updateJobQuery);
			preparedStatement.setLong(1, nextExecutionTime);
			preparedStatement.setLong(2, jobId);

			preparedStatement.executeUpdate();
		}
		catch(Exception e)
		{
			LOGGER.log(Level.SEVERE, "Exception occurred", e);
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

			ConfigurationTableUtil.delete((DigestUtils.sha1Hex(String.valueOf(id))));

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

	public static void addJobInQueue(String selectQuery)
	{
		try(Connection connection = DBUtil.getServerDBConnection())
		{
			PreparedStatement preparedStatement = connection.prepareStatement(selectQuery);

			ResultSet resultSet = preparedStatement.executeQuery();

			while(resultSet.next())
			{
				long jobId = resultSet.getLong("id");
				long executionTime = resultSet.getLong("scheduled_time");
				int dayInterval = resultSet.getInt("day_interval");
				if(DateUtil.getCurrentTimeInMillis() > executionTime)
				{
					executionTime = getNextExecutionTimeFromPreviousScheduleTime(executionTime, dayInterval);
					updateNextExecutionTime(jobId, executionTime);
				}

				RefreshManager.addJobInQueue(jobId);
			}
		}
		catch(Exception e)
		{
			LOGGER.log(Level.SEVERE, "Exception occurred while adding jobs in queue", e);
		}
	}
}
