package com.server.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.FilterOutputStream;
import java.net.ProxySelector;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.io.IOUtils;
import org.apache.tomcat.dbcp.dbcp2.BasicDataSource;

import com.server.db.DBUtil;
import com.server.file.FileManager;
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

		DBUtil.initialiseDataSource();

		RefreshManager.init();

		FileManager.copyUploads();

		Util.init(sce.getServletContext());

		LOGGER.log(Level.INFO, "Context Initialised at {0}", Util.getFormattedCurrentTime());
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce)
	{
		LOGGER.log(Level.INFO, "Context Destroyed at {0}", Util.getFormattedCurrentTime());
	}
}
