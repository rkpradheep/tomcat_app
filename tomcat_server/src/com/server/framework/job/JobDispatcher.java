package com.server.framework.job;

import com.server.framework.security.LoginUtil;
import com.server.framework.security.ThrottleHandler;

public class JobDispatcher implements Task
{
	@Override public void run(String value) throws Exception
	{
		try
		{
			ThrottleHandler.removeExpiredIPLockingAndThrottleMeta();
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
