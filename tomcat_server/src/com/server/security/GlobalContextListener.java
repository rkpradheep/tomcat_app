package com.server.security;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;

public class GlobalContextListener implements LifecycleListener
{
	private static final Logger LOGGER = Logger.getLogger(GlobalContextListener.class.getName());

	@Override public void lifecycleEvent(LifecycleEvent lifecycleEvent)
	{
		if(Lifecycle.BEFORE_START_EVENT.equals(lifecycleEvent.getType()))
		{
			Configuration.load();
			DBUtil.initialiseDataSource();
		}
	}
}