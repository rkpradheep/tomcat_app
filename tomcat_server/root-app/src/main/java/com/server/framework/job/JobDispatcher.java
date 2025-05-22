package com.server.framework.job;

import com.server.framework.common.DateUtil;
import com.server.framework.persistence.Criteria;
import com.server.framework.persistence.SelectQuery;
import com.server.framework.security.LoginUtil;
import com.server.framework.security.ThrottleHandler;
import com.server.table.constants.JOB;

public class JobDispatcher
{
	public static void run() throws Exception
	{
		try
		{
			ThrottleHandler.removeExpiredIPLockingAndThrottleMeta();
			LoginUtil.deleteExpiredSessions();

			SelectQuery selectQuery = new SelectQuery(JOB.TABLE);
			selectQuery.setCriteria(new Criteria(JOB.TABLE, JOB.SCHEDULEDTIME, DateUtil.getCurrentTimeInMillis() + 5000, Criteria.Constants.LESS_THAN));
			JobUtil.addJobInQueue(selectQuery);
		}
		finally
		{
			JobUtil.scheduleJob(JobDispatcher::run, 5);
		}
	}
}
