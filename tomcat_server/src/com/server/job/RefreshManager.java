package com.server.job;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Objects;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.server.security.Configuration;
import com.server.security.DBUtil;
import com.server.security.SecurityUtil;

public class RefreshManager
{
	private static ThreadPoolExecutor executor = null;
	private static int threadNumber = 1;
	private static DelayQueue queue;
	private static final Logger LOGGER = Logger.getLogger(RefreshManager.class.getName());

	public static void init()
	{
		ThreadFactory tf = run -> new Thread(run, "refresh-manager-" + RefreshManager.threadNumber++);
		queue = new DelayQueue();
		executor = new ThreadPoolExecutor(1, 2, 1L, TimeUnit.MINUTES, queue, tf, new ThreadPoolExecutor.CallerRunsPolicy());
		executor.prestartAllCoreThreads();

		if(Configuration.getBoolean("can.add.job.dispatcher"))
		{
			addJobInQueue(-1L, JobDispatcher::new, null, 1);
		}
		else
		{
			String pendingJobQuery = "SELECT * FROM Job";
			addJobInQueue(pendingJobQuery);
		}
	}

	public static void shutDown()
	{
		executor.shutdownNow();
	}

	public static void addJobInQueue(Long jobID, Supplier<Task> taskHandler, String data, int seconds)
	{
		addJobInQueue(jobID, taskHandler, data, seconds * 1000L);
	}

	public static void addJobInQueue(Long jobID, Supplier<Task> taskHandler, String data, long milliseconds)
	{
		queue.add(new RefreshManager.RefreshElement(jobID, taskHandler, data, System.currentTimeMillis() + milliseconds));
	}

	public static void addJobInQueue(CustomRunnable runnable, int seconds)
	{
		queue.add(new RefreshManager.RefreshElement(runnable, System.currentTimeMillis() + (seconds * 1000L)));
	}

	public static void addJobInQueue(String selectQuery)
	{
		try(Connection connection = DBUtil.getServerDBConnection())
		{
			PreparedStatement preparedStatement = connection.prepareStatement(selectQuery);

			ResultSet resultSet = preparedStatement.executeQuery();

			while(resultSet.next())
			{
				String task = resultSet.getString("task_name");
				String data = resultSet.getString("data");
				long scheduledTime = resultSet.getLong("scheduled_time");
				long jobId = resultSet.getLong("id");
				long delay = (scheduledTime - System.currentTimeMillis());
				delay = Math.max(delay, 0);

				addJobInQueue(jobId, TaskEnum.getHandler(task), data, delay);
			}
		}
		catch(Exception e)
		{
			LOGGER.log(Level.SEVERE, "Exception occurred while adding jobs in queue", e);
		}
	}

	private static class RefreshElement implements Delayed, Runnable
	{
		Long jobId;
		Supplier<Task> taskSupplier;
		String data;
		long time;
		CustomRunnable runnable;

		RefreshElement(Long jobId, Supplier<Task> taskSupplier, String data, long time)
		{
			this.jobId = jobId;
			this.taskSupplier = taskSupplier;
			this.data = data;
			this.time = time;
		}

		RefreshElement(CustomRunnable runnable, long time)
		{
			this.runnable = runnable;
			this.time = time;
		}

		public void run()
		{
			try
			{
				if(Objects.nonNull(runnable))
				{
					runnable.run();
					return;
				}

				Task task = taskSupplier.get();

				if(jobId != -1)
				{
					LOGGER.log(Level.INFO, "Executing task {0} with ID {1} at {2}", new Object[] {task.getClass().getName(), jobId, SecurityUtil.getFormattedCurrentTime()});
				}

				task.run(data);
			}
			catch(Exception e)
			{
				LOGGER.log(Level.INFO, "Exception during refresh job", e);
			}
			finally
			{
				if(Objects.nonNull(jobId))
				{
					JobUtil.deleteJob(jobId);
				}
			}

		}

		@Override
		public int compareTo(Delayed delayed)
		{
			return Long.compare(this.time, ((RefreshElement) delayed).time);
		}

		@Override
		public long getDelay(TimeUnit tu)
		{
			long delay = this.time - System.currentTimeMillis();
			return tu.convert(delay, TimeUnit.MILLISECONDS);
		}
	}
}
