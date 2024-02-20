package com.server.tomcat;

import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;

import com.server.security.Configuration;
import com.server.security.DBUtil;

public class LifeCycleListener implements LifecycleListener
{
	@Override public void lifecycleEvent(LifecycleEvent event)
	{
		String type = event.getType();

		if(Lifecycle.STOP_EVENT.equals(type))
		{
			DBUtil.closeDataSource();
		}
		else if(Lifecycle.START_EVENT.equals(type))
		{
			Configuration.load();
			DBUtil.initialiseDataSource();
		}

	}
}
