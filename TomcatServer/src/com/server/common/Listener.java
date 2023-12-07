package com.server.common;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import com.server.common.Util;

public class Listener implements HttpSessionListener
{
	public void sessionCreated(HttpSessionEvent se)
	{
	}

	public void sessionDestroyed(HttpSessionEvent se)
	{
		try
		{
			Util.postMessageToBot("Session Destroyed. \nSession ID : " + se.getSession().getId() + "\nCreated Time : " + Util.getFormattedCurrentTime(se.getSession().getCreationTime()) + "\nLast Accessed Time : " + Util.getFormattedCurrentTime(se.getSession().getLastAccessedTime()) + "\nMax Inactive Minute(s) : " + se.getSession().getMaxInactiveInterval() / 60);
		}
		catch(Exception e)
		{
		}
	}

}
