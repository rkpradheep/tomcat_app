package com.server.common;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;

public class Configuration
{
	private static final ConcurrentHashMap<String, String> appProps = new ConcurrentHashMap<>();
	private static final Logger LOGGER = Logger.getLogger(Configuration.class.getName());

	public static String getProperty(String key)
	{
		return appProps.getOrDefault(key, "").trim();
	}

	public static void load(ServletContext servletContext)
	{
		try
		{
			String logMessage = "Going to load : app.properties";
			LOGGER.log(Level.INFO, logMessage);
			InputStream is = servletContext.getResourceAsStream("/WEB-INF/conf/app.properties");
			load(is);
		}
		catch(Exception e)
		{
			LOGGER.log(Level.SEVERE, "Exception occurred when loading property file", e);
		}
	}

	private static void load(InputStream is) throws IOException
	{
		try(is)
		{
			Properties props = new Properties();
			props.load(is);
			Enumeration<?> elements = props.propertyNames();

			while(elements.hasMoreElements())
			{
				String key = (String) elements.nextElement();
				String val = props.getProperty(key);
				appProps.put(key, val);
			}

			LOGGER.log(Level.INFO, "Properties file has been loaded.");
		}

	}
}
