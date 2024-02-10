package com.server.job;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.server.security.DBUtil;
import com.server.security.LoginUtil;
import com.server.security.ThrottleHandler;

public class JobDispatcher implements Task
{
	@Override public void run(String value) throws Exception
	{
		try
		{
			ThrottleHandler.removeExpiredIPLocking();
			LoginUtil.deleteExpiredSessions();

			String selectQuery = "SELECT * FROM Job where scheduled_time <= " + (System.currentTimeMillis() + 5000);

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
					int delay = (int) (scheduledTime - System.currentTimeMillis());
					delay = Math.max(delay, 0);

					RefreshManager.addJobInQueue(jobId, TaskEnum.getHandler(task), data, delay / 10000);
				}
			}
		}
		finally
		{
			RefreshManager.addJobInQueue(-1L, JobDispatcher::new, null, 5);
		}
	}
}
