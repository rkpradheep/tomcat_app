package com.server.framework.listener;

import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;

import com.server.framework.common.Util;
import com.server.framework.security.SecurityUtil;

public class SessionListener implements HttpSessionListener
{
	public void sessionCreated(HttpSessionEvent se)
	{
	}

	public void sessionDestroyed(HttpSessionEvent se)
	{
		try
		{
			Util.postMessageToBot("Session Destroyed. \nSession ID : " + se.getSession().getId() + "\nCreated Time : " + SecurityUtil.getFormattedTime(se.getSession().getCreationTime()) + "\nLast Accessed Time : " + SecurityUtil.getFormattedTime(se.getSession().getLastAccessedTime()) + "\nMax Inactive Minute(s) : " + se.getSession().getMaxInactiveInterval() / 60);
		}
		catch(Exception e)
		{
		}
	}

}
