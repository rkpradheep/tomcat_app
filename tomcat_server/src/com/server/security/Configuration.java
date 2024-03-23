package com.server.security;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;

public class Configuration
{
	private static final Map<String, String> appProps = new ConcurrentHashMap<>();
	private static final Logger LOGGER = Logger.getLogger(Configuration.class.getName());

	public static String getProperty(String key)
	{
		return appProps.getOrDefault(key.trim(), StringUtils.EMPTY).trim();
	}

	public static boolean getBoolean(String key)
	{
		return Boolean.parseBoolean(appProps.getOrDefault(key, "false"));
	}

	public static void setProperty(String key, String value)
	{
		appProps.put(key.trim(), value.trim());
	}

	public static List<String> getPropertyList()
	{
		return new ArrayList<>(appProps.keySet());
	}

	public static void load()
	{
		try
		{
			String logMessage = "Going to load : app.properties";
			LOGGER.log(Level.INFO, logMessage);
			InputStream is = Configuration.class.getResourceAsStream("app.properties");
			load(is);
		}
		catch(Exception e)
		{
			LOGGER.log(Level.SEVERE, "Exception occurred when loading property file", e);
		}
	}

	private static void load(InputStream is) throws IOException
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
		appProps.put("production", System.getenv("PRODUCTION"));

		LOGGER.log(Level.INFO, "Properties file has been loaded.");

	}
}
