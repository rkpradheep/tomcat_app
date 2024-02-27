package com.server.security.listener;

import java.lang.reflect.Constructor;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.lang3.StringUtils;

import com.server.security.Configuration;
import com.server.security.DBUtil;
import com.server.security.SecurityUtil;

public class ContextListener implements ServletContextListener
{
	private static final Logger LOGGER = Logger.getLogger(ContextListener.class.getName());

	@Override
	public void contextInitialized(ServletContextEvent sce)
	{
		String contextName = sce.getServletContext().getServletContextName();

		LOGGER.log(Level.INFO, "Initializing Context for {0} at {1}", new Object[] {contextName, SecurityUtil.getFormattedCurrentTime()});

		Configuration.load();

		DBUtil.initialiseDataSource();

		if(StringUtils.equals(contextName, "Main"))
		{
			fireContextEvent(true, sce);
		}

		LOGGER.log(Level.INFO, "Context Initialised for {0} at {1}", new Object[] {contextName, SecurityUtil.getFormattedCurrentTime()});
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce)
	{
		String contextName = sce.getServletContext().getServletContextName();

		DBUtil.closeDataSource();

		if(StringUtils.equals(contextName, "Main"))
		{
			fireContextEvent(false, sce);
		}

		LOGGER.log(Level.INFO, "Context Destroyed for {0} at {1}", new Object[] {contextName, SecurityUtil.getFormattedCurrentTime()});
	}

	private void fireContextEvent(boolean isInit, ServletContextEvent servletContextEvent)
	{
		try
		{
			Constructor<?> constructor = Class.forName("com.server.common.ContextListener").getDeclaredConstructor();
			Listener listener = (Listener) constructor.newInstance();
			if(isInit)
			{
				listener.contextInitialized(servletContextEvent);
			}
			else
			{
				listener.contextDestroyed(servletContextEvent);
			}
		}
		catch(Exception e)
		{
			LOGGER.log(Level.SEVERE, "Exception occurred", e);
		}
	}
}
