package com.server.common;

import java.net.ProxySelector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;

import com.server.file.FileManager;

import com.server.job.RefreshManager;
import com.server.security.listener.Listener;
import com.server.security.SecurityUtil;

public class ContextListener implements Listener
{
	private static final Logger LOGGER = Logger.getLogger(ContextListener.class.getName());

	@Override
	public void contextInitialized(ServletContextEvent sce)
	{
		RefreshManager.init();

		ProxySelector.setDefault(new ProxySelectorExtension());

		FileManager.copyUploads();

		//ProxyServer.init();
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce)
	{
		RefreshManager.shutDown();

		//ProxyServer.shutDown();
	}
}
