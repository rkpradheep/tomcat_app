package com.server.common;

import java.net.ProxySelector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.server.file.FileManager;
import com.server.job.RefreshManager;
import com.server.security.Configuration;
import com.server.security.DBUtil;
import com.server.security.SecurityUtil;

public class ContextListener implements ServletContextListener
{
	private static final Logger LOGGER = Logger.getLogger(ContextListener.class.getName());

	@Override
	public void contextInitialized(ServletContextEvent sce)
	{
		LOGGER.log(Level.INFO, "Initializing Context at {0}", SecurityUtil.getFormattedCurrentTime());

		ProxySelector.setDefault(new ProxySelectorExtension());

		Configuration.load();

		DBUtil.initialiseDataSource();

		//ProxyServer.init();

		RefreshManager.init();

		FileManager.copyUploads();

		LOGGER.log(Level.INFO, "Context Initialised at {0}", SecurityUtil.getFormattedCurrentTime());
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce)
	{
		RefreshManager.shutDown();

		//ProxyServer.shutDown();

		LOGGER.log(Level.INFO, "Context Destroyed at {0}", SecurityUtil.getFormattedCurrentTime());
	}
}
