package com.server.framework.job;

public class JobMeta
{
	private final long id;
	private final String data;
	private final boolean isRecurring;
	private final String taskName;
	private final long scheduledTime;
	private final int intervalInDays;

	private JobMeta(long id, String data, boolean isRecurring, String taskName, long scheduledTime, int intervalInDays)
	{
		this.id = id;
		this.data = data;
		this.isRecurring = isRecurring;
		this.taskName = taskName;
		this.scheduledTime = scheduledTime;
		this.intervalInDays = intervalInDays;
	}

	private CustomRunnable runnable;

	public CustomRunnable getRunnable()
	{
		return runnable;
	}

	public int getIntervalInDays()
	{
		return intervalInDays;
	}

	public long getScheduledTime()
	{
		return scheduledTime;
	}

	public String getTaskName()
	{
		return taskName;
	}

	public long getId()
	{
		return id;
	}

	public String getData()
	{
		return data;
	}

	public boolean isRecurring()
	{
		return isRecurring;
	}

	public static class Builder
	{
		private long id;
		private String data;
		private boolean isRecurring;
		private String taskName = "lambda";
		private long scheduledTime;
		private int intervalInDays;
		private CustomRunnable runnable;

		public Builder setId(long id)
		{
			this.id = id;
			return this;
		}

		public Builder setRunnable(CustomRunnable runnable)
		{
			this.runnable = runnable;
			return this;
		}

		public Builder setIntervalInDays(int intervalInDays)
		{
			this.intervalInDays = intervalInDays;
			return this;
		}

		public Builder setScheduledTime(long scheduledTime)
		{
			this.scheduledTime = scheduledTime;
			return this;
		}

		public Builder setTaskName(String taskName)
		{
			this.taskName = taskName;
			return this;
		}

		public Builder setRecurring(boolean recurring)
		{
			isRecurring = recurring;
			return this;
		}

		public Builder setData(String data)
		{
			this.data = data;
			return this;
		}

		public JobMeta build()
		{
			return new JobMeta(this.id, this.data, this.isRecurring, this.taskName, this.scheduledTime, this.intervalInDays);
		}

	}
}
