package com.server.framework.job;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.codec.digest.DigestUtils;

import com.server.framework.common.Configuration;
import com.server.framework.common.ConfigurationTableUtil;
import com.server.framework.common.DateUtil;
import com.server.framework.persistence.Criteria;
import com.server.framework.persistence.DataAccess;
import com.server.framework.persistence.DataObject;
import com.server.framework.persistence.Row;
import com.server.framework.persistence.SelectQuery;
import com.server.framework.persistence.UpdateQuery;
import com.server.table.constants.JOB;

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

		try
		{
			DataAccess.Transaction.begin();

			long executionTime = DateUtil.getCurrentTimeInMillis() + milliSeconds;

			DataObject dataObject = new DataObject();
			Row row = new Row(JOB.TABLE);
			row.set(JOB.TASKNAME, taskName);
			row.set(JOB.DATA, data);
			row.set(JOB.SCHEDULEDTIME, executionTime);
			row.set(JOB.DAYINTERVAL, dayInterval);
			row.set(JOB.ISRECURRING, isRecurring);

			dataObject.addRow(row);
			DataAccess.add(dataObject);

			long id = (long) dataObject.getRows().get(0).get(JOB.ID);
			if(isRecurring)
			{
				ConfigurationTableUtil.setValue(DigestUtils.sha1Hex(String.valueOf(id)), String.valueOf(id));
			}

			DataAccess.Transaction.commit();

			if(!Configuration.getBoolean("can.add.job.dispatcher"))
			{
				RefreshManager.addJobInQueue(id);
			}

			LOGGER.log(Level.INFO, "Job added with ID {0} for task {1} with delay {2} seconds. Formatted execution time : {3}", new Object[] {Long.toString(id), taskName, milliSeconds / 1000, DateUtil.getFormattedTime(executionTime)});
			return id;
		}
		catch(Exception e)
		{
			LOGGER.log(Level.SEVERE, "Exception occurred", e);
			DataAccess.Transaction.rollback();
			throw e;
		}
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

		try
		{
			SelectQuery selectQuery = new SelectQuery(JOB.TABLE);
			selectQuery.setCriteria(new Criteria(JOB.TABLE, JOB.ID, jobId, Criteria.Constants.EQUAL));
			DataObject dataObject = DataAccess.get(selectQuery);

			Row row = dataObject.getRows().get(0);

			String task = (String) row.get(JOB.TASKNAME);
			String data = (String) row.get(JOB.DATA);
			long scheduledTime = (long) row.get(JOB.SCHEDULEDTIME);
			boolean isRecurring = (int) row.get(JOB.ISRECURRING) == 1;
			int dayInterval = (int) row.get(JOB.DAYINTERVAL);

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
		try
		{
			UpdateQuery updateQuery = new UpdateQuery(JOB.TABLE);
			updateQuery.setValue(JOB.SCHEDULEDTIME, nextExecutionTime);
			updateQuery.setCriteria(new Criteria(JOB.TABLE, JOB.ID, jobId, Criteria.Constants.EQUAL));

			DataAccess.update(updateQuery);
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

		try
		{
			DataAccess.Transaction.begin();

			Criteria criteria = new Criteria(JOB.TABLE, JOB.ID, id, Criteria.Constants.EQUAL);
			DataAccess.delete(JOB.TABLE, criteria);

			ConfigurationTableUtil.delete((DigestUtils.sha1Hex(String.valueOf(id))));

			DataAccess.Transaction.commit();

			LOGGER.log(Level.INFO, "Job with ID {0} is deleted", new Object[] {id});
		}
		catch(Exception e)
		{
			DataAccess.Transaction.rollback();
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

	public static void addJobInQueue(SelectQuery selectQuery)
	{
		try
		{
			List<Row> rowList = DataAccess.get(selectQuery).getRows();

			for(Row row : rowList)
			{
				long jobId = (long) row.get(JOB.ID);
				long executionTime = (long) row.get(JOB.SCHEDULEDTIME);
				int dayInterval = (int) row.get(JOB.DAYINTERVAL);
				if(dayInterval != -1 && DateUtil.getCurrentTimeInMillis() > executionTime)
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
