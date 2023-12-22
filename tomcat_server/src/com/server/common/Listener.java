package com.server.common;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

public class Listener implements HttpSessionListener
{
	public void sessionCreated(HttpSessionEvent se)
	{
	}

	public void sessionDestroyed(HttpSessionEvent se)
	{
		try
		{
			Util.postMessageToBot("Session Destroyed. \nSession ID : " + se.getSession().getId() + "\nCreated Time : " + Util.getFormattedTime(se.getSession().getCreationTime()) + "\nLast Accessed Time : " + Util.getFormattedTime(se.getSession().getLastAccessedTime()) + "\nMax Inactive Minute(s) : " + se.getSession().getMaxInactiveInterval() / 60);
		}
		catch(Exception e)
		{
		}
	}

}
