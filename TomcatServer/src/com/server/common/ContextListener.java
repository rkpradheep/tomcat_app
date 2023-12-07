package com.server.common;

import java.net.ProxySelector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.server.job.RefreshManager;
import com.server.proxy.ProxyServer;

public class ContextListener implements ServletContextListener
{
	private static final Logger LOGGER = Logger.getLogger(ContextListener.class.getName());

	@Override
	public void contextInitialized(ServletContextEvent sce)
	{
		ProxySelector.setDefault(new ProxySelectorExtension());
		Configuration.load(sce.getServletContext());
		new Thread(ProxyServer::start).start();
		RefreshManager.init();
		LOGGER.log(Level.INFO, "Context Initialised at {0}", Util.getFormattedCurrentTime());
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce)
	{
		LOGGER.log(Level.INFO, "Context Destroyed at {0}", Util.getFormattedCurrentTime());
	}
}
