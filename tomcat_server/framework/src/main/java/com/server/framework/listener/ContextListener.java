package com.server.framework.listener;

import jakarta.servlet.ServletContextListener;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.server.framework.common.Configuration;
import com.server.framework.common.DateUtil;
import com.server.framework.common.FileManager;
import com.server.framework.common.ProxyServer;
import com.server.framework.job.RefreshManager;
import com.server.framework.persistence.DBUtil;

public class ContextListener implements ServletContextListener
{
	private static final Logger LOGGER = Logger.getLogger(ContextListener.class.getName());

	@Override
	public void contextInitialized(jakarta.servlet.ServletContextEvent sce)
	{
		LOGGER.log(Level.INFO, "Initializing Context started");

		Configuration.load();

		DBUtil.initialiseDataSource();

		RefreshManager.init();

		//ProxySelector.setDefault(new ProxySelectorExtension());

		FileManager.copyUploads();

		if(Configuration.getBoolean("proxy.server"))
		{
			ProxyServer.init(Configuration.getProperty("proxy.port"), Configuration.getProperty("proxy.user"),  Configuration.getProperty("proxy.password"));
		}

		LOGGER.log(Level.INFO, "Context Initialised for at {0}", new Object[] {DateUtil.getFormattedCurrentTime()});
	}
	@Override
	public void contextDestroyed(jakarta.servlet.ServletContextEvent sce)
	{
		DBUtil.closeDataSource();

		RefreshManager.shutDown();

		if(Configuration.getBoolean("proxy.server"))
		{
			ProxyServer.shutDown();
		}

		LOGGER.log(Level.INFO, "Context Destroyed at {1}", new Object[] {DateUtil.getFormattedCurrentTime()});
	}
}
