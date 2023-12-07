package com.server.job;

import java.util.Arrays;

public enum TaskNameEnum
{
	REMINDER("reminder", "Invitation Reminder Job", ReminderTask.class);

	private String taskName;
	private String taskDisplayName;
	private Class<? extends Task> clazz;

	public String getTaskName()
	{
		return taskName;
	}

	public String getTaskDisplayName()
	{
		return taskDisplayName;
	}

	TaskNameEnum(String taskName, String taskDisplayName, Class<? extends Task> clazz)
	{
		this.taskName = taskName;
		this.taskDisplayName = taskDisplayName;
		this.clazz = clazz;
	}


	public static Class<? extends Task> getClazz(String taskName)
	{
		return Arrays.stream(values()).filter(taskNameEnum -> taskNameEnum.taskName.equals(taskName)).map(taskNameEnum -> taskNameEnum.clazz).findFirst().get();
	}
}
