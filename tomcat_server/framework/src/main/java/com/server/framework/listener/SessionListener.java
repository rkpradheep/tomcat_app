package com.server.framework.listener;

import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;

import java.util.logging.Logger;

import com.server.framework.common.DateUtil;

public class SessionListener implements HttpSessionListener
{
	private static final Logger LOGGER = Logger.getLogger(SessionListener.class.getName());
	public void sessionCreated(HttpSessionEvent se)
	{
	}

	public void sessionDestroyed(HttpSessionEvent se)
	{
		try
		{
			String message = "Session Destroyed. \nSession ID : " + se.getSession().getId() + "\nCreated Time : " + DateUtil.getFormattedTime(se.getSession().getCreationTime()) + "\nLast Accessed Time : " + DateUtil.getFormattedTime(se.getSession().getLastAccessedTime()) + "\nMax Inactive Minute(s) : " + se.getSession().getMaxInactiveInterval() / 60;
			LOGGER.info(message);
			//Util.postMessageToBot();
		}
		catch(Exception e)
		{
		}
	}

}
