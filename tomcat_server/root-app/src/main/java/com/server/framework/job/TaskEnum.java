package com.server.framework.job;

import java.util.Arrays;

public enum TaskEnum
{
	MAIL("mail", "Mail Scheduler", MailTask::run, true),
	REMINDER("reminder", "Invitation Reminder Job", ReminderTask::run, false);

	private final String taskName;
	private final String taskDisplayName;
	private final CustomRunnable runnable;
	private final boolean needDBEntry;

	public boolean isNeedDBEntry()
	{
		return needDBEntry;
	}

	public String getTaskName()
	{
		return taskName;
	}

	public String getTaskDisplayName()
	{
		return taskDisplayName;
	}

	public CustomRunnable getRunnable()
	{
		return runnable;
	}

	TaskEnum(String taskName, String taskDisplayName, CustomRunnable runnable, boolean needDBEntry)
	{
		this.taskName = taskName;
		this.taskDisplayName = taskDisplayName;
		this.runnable = runnable;
		this.needDBEntry = needDBEntry;
	}

	public static TaskEnum getTask(String taskName) throws Exception
	{
		return Arrays.stream(values()).filter(taskEnum -> taskEnum.taskName.equals(taskName))
			.findFirst().orElseThrow(() -> new Exception("task with given name doesn't exist"));
	}

	public static CustomRunnable getRunnable(String taskName) throws Exception
	{
		return getTask(taskName).getRunnable();
	}
}
