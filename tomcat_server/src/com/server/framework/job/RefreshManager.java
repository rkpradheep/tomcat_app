package com.server.framework.job;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.server.framework.common.Configuration;
import com.server.framework.common.DateUtil;

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
			addJobInQueue(-1L);
		}
		else
		{
			String pendingJobQuery = "SELECT * FROM Job";
			JobUtil.addJobInQueue(pendingJobQuery);
		}
	}

	public static void shutDown()
	{
		executor.shutdownNow();
	}

	public static void removeJobFromQueue(long jobID)
	{
		Iterator<RefreshElement> refreshManagerIterator = queue.iterator();
		while(refreshManagerIterator.hasNext())
		{
			RefreshElement refreshElement = refreshManagerIterator.next();
			if(refreshElement.jobMeta.getId() == jobID)
			{
				queue.remove(refreshElement);
				LOGGER.info("Job with ID " + jobID + " removed from queue");
			}
		}
	}

	public static void addJobInQueue(Long jobID)
	{
		queue.add(new RefreshElement(jobID));
	}

	public static void addJobInQueue(CustomRunnable runnable, String data, long millisecond)
	{
		queue.add(new RefreshElement(runnable, data, DateUtil.getCurrentTimeInMillis() + millisecond));
	}

	private static class RefreshElement implements Delayed, Runnable
	{
		JobMeta jobMeta;

		RefreshElement(Long jobId)
		{
			jobMeta = JobUtil.getJobMeta(jobId);
		}

		RefreshElement(CustomRunnable runnable, String data, long time)
		{
			jobMeta = new JobMeta.Builder()
				.setId(-1L)
				.setData(data)
				.setRecurring(false)
				.setScheduledTime(time)
				.setRunnable(runnable)
				.build();
		}

		@Override public void run()
		{
			try
			{
				JobUtil.SCHEDULER_TL.set(jobMeta);
				LOGGER.info("Job started for runnable " + jobMeta.getTaskName());
				jobMeta.getRunnable().run();
			}
			catch(Exception e)
			{
				LOGGER.log(Level.INFO, "Exception during refresh job", e);
			}
			finally
			{
				JobUtil.handlePostProcess(jobMeta);
			}

		}

		@Override
		public int compareTo(Delayed delayed)
		{
			return Long.compare(jobMeta.getScheduledTime(), ((RefreshElement) delayed).jobMeta.getScheduledTime());
		}

		@Override
		public long getDelay(TimeUnit tu)
		{
			long delay = jobMeta.getScheduledTime() - DateUtil.getCurrentTimeInMillis();
			return tu.convert(delay, TimeUnit.MILLISECONDS);
		}
	}
}
