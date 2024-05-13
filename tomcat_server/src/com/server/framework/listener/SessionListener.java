package com.server.framework.listener;

import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;

import com.server.framework.common.DateUtil;
import com.server.framework.common.Util;

public class SessionListener implements HttpSessionListener
{
	public void sessionCreated(HttpSessionEvent se)
	{
	}

	public void sessionDestroyed(HttpSessionEvent se)
	{
		try
		{
			Util.postMessageToBot("Session Destroyed. \nSession ID : " + se.getSession().getId() + "\nCreated Time : " + DateUtil.getFormattedTime(se.getSession().getCreationTime()) + "\nLast Accessed Time : " + DateUtil.getFormattedTime(se.getSession().getLastAccessedTime()) + "\nMax Inactive Minute(s) : " + se.getSession().getMaxInactiveInterval() / 60);
		}
		catch(Exception e)
		{
		}
	}

}
