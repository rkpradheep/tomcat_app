package com.server.job;

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

			String pendingJobQuery = "SELECT * FROM Job where scheduled_time <= " + (System.currentTimeMillis() + 5000);
			RefreshManager.addJobInQueue(pendingJobQuery);
		}
		finally
		{
			RefreshManager.addJobInQueue(-1L, JobDispatcher::new, null, 5);
		}
	}
}
