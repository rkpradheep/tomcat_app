package com.server.common;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import com.server.security.SecurityUtil;

public class Listener implements HttpSessionListener
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
