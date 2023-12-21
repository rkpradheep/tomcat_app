package com.server.job;

import java.util.Arrays;
import java.util.function.Supplier;

public enum TaskEnum
{
	MAIL("mail", "Mail Scheduler", MailTask::new),
	REMINDER("reminder", "Invitation Reminder Job", ReminderTask::new);

	private final String taskName;
	private final String taskDisplayName;
	private final Supplier<Task> taskHandler;

	public String getTaskName()
	{
		return taskName;
	}

	public String getTaskDisplayName()
	{
		return taskDisplayName;
	}

	public Supplier<Task> getTaskHandler()
	{
		return taskHandler;
	}

	TaskEnum(String taskName, String taskDisplayName, Supplier<Task> taskHandler)
	{
		this.taskName = taskName;
		this.taskDisplayName = taskDisplayName;
		this.taskHandler = taskHandler;
	}

	public static Supplier<Task> getHandler(String taskName) throws Exception
	{
		return Arrays.stream(values()).filter(taskEnum -> taskEnum.taskName.equals(taskName))
			.map(TaskEnum::getTaskHandler)
			.findFirst().orElseThrow(() -> new Exception("task with given name doesn't exist"));
	}
}
