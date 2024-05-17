package com.server.framework.job;

import com.server.framework.common.DateUtil;
import com.server.framework.security.LoginUtil;
import com.server.framework.security.ThrottleHandler;

public class JobDispatcher
{
	public static void run() throws Exception
	{
		try
		{
			ThrottleHandler.removeExpiredIPLockingAndThrottleMeta();
			LoginUtil.deleteExpiredSessions();

			String pendingJobQuery = "SELECT * FROM Job where scheduled_time <= " + (DateUtil.getCurrentTimeInMillis() + 5000);
			JobUtil.addJobInQueue(pendingJobQuery);
		}
		finally
		{
			JobUtil.scheduleJob(JobDispatcher::run, 5);
		}
	}
}
