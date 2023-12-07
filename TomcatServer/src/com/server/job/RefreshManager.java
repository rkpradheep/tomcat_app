package com.server.job;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.server.common.Util;

public class RefreshManager
{
	private static ThreadPoolExecutor executor = null;
	private static int threadNumber = 1;
	private static DelayQueue queue;
	private static final Logger LOGGER = Logger.getLogger(RefreshManager.class.getName());
	private static final List<String> SCHEDULED_JOBS_LIST = new ArrayList<>();

	public RefreshManager()
	{
	}

	public static void addJob(Class<? extends Task> clazz, String data, int seconds) throws Exception
	{
		if(SCHEDULED_JOBS_LIST.contains(clazz.getName()))
		{
			throw new Exception("Job already scheduled");
		}
		LOGGER.log(Level.INFO, "Job added for task {0} with delay {1} seconds at {2}", new Object[] {clazz.getName(), seconds, Util.getFormattedCurrentTime()});
		queue.add(new RefreshManager.RefreshElement(clazz, data, System.currentTimeMillis() + (seconds * 1000L)));
		SCHEDULED_JOBS_LIST.add(clazz.getName());
	}

	public static void init()
	{
		ThreadFactory tf = run -> new Thread(run, "refresh-manager-" + RefreshManager.threadNumber++);
		queue = new DelayQueue();
		executor = new ThreadPoolExecutor(1, 1, 1L, TimeUnit.MINUTES, queue, tf, new ThreadPoolExecutor.CallerRunsPolicy());
		executor.prestartAllCoreThreads();
	}

	private static void refresh(Class<? extends Task> clazz, String data) throws Exception
	{
		try
		{
			LOGGER.log(Level.INFO, "Executing task {0} at {1}", new Object[] {clazz.getName(), Util.getFormattedCurrentTime()});

			Task task = clazz.newInstance();
			SCHEDULED_JOBS_LIST.remove(clazz.getName());
			task.run(data);
		}
		catch(Exception var6)
		{
			LOGGER.log(Level.INFO, "Exception during refresh job for task - " + clazz.getName() + " ", var6);
		}

	}

	private static class RefreshElement implements Delayed, Runnable
	{
		Class<? extends Task> clazz;
		String data;
		long time;

		RefreshElement(Class<? extends Task> clazz, String data, long time)
		{
			this.clazz = clazz;
			this.data = data;
			this.time = time;
		}

		public void run()
		{
			try
			{
				RefreshManager.refresh(this.clazz, this.data);
			}
			catch(Exception var2)
			{
				RefreshManager.LOGGER.log(Level.SEVERE, "Exception occurred while refreshing task - " + clazz.getName() + " ", var2);
			}

		}

		public int compareTo(Delayed obj)
		{
			RefreshElement element = (RefreshElement) obj;
			if(element.time - this.time > 0L)
			{
				return -1;
			}
			else
			{
				return element.time - this.time == 0L ? 0 : 1;
			}
		}

		public long getDelay(TimeUnit tu)
		{
			Long delay = this.time - System.currentTimeMillis();
			//LOGGER.log(Level.INFO, "Get Delay executed at {0} and delay value is {1}", new Object[] {com.server.common.Util.getFormattedCurrentTime(), (delay / 1000)});
			return tu.convert(delay, TimeUnit.MILLISECONDS);

		}
	}
}
