package com.server.job;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.server.common.Util;

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
		executor = new ThreadPoolExecutor(1, 5, 1L, TimeUnit.MINUTES, queue, tf, new ThreadPoolExecutor.CallerRunsPolicy());
		executor.prestartAllCoreThreads();

		addJobInQueue(-1L, JobDispatcher::new, null, 1);
	}
	public static void shutDown()
	{
		executor.shutdownNow();
	}

	public static void addJobInQueue(Long jobID, Supplier<Task> taskHandler, String data, int seconds)
	{
		queue.add(new RefreshManager.RefreshElement(jobID, taskHandler, data, System.currentTimeMillis() + (seconds * 1000L)));
	}

	private static class RefreshElement implements Delayed, Runnable
	{
		Long jobId;
		Supplier<Task> taskSupplier;
		String data;
		long time;

		RefreshElement(Long jobId, Supplier<Task> taskSupplier, String data, long time)
		{
			this.jobId = jobId;
			this.taskSupplier = taskSupplier;
			this.data = data;
			this.time = time;
		}

		public void run()
		{
			try
			{
				Task task = taskSupplier.get();

				if(jobId != -1)
				{
					LOGGER.log(Level.INFO, "Executing task {0} with ID {1} at {2}", new Object[] {task.getClass().getName(), jobId, Util.getFormattedCurrentTime()});
				}

				task.run(data);
				JobUtil.deleteJob(jobId);
			}
			catch(Exception var6)
			{
				LOGGER.log(Level.INFO, "Exception during refresh job for task - " + taskSupplier.get().getClass().getName() + " ", var6);
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
