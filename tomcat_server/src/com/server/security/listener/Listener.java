package com.server.security.listener;

import javax.servlet.ServletContextEvent;

public interface Listener
{
	void contextInitialized(ServletContextEvent sce);
	void contextDestroyed(ServletContextEvent sce);
}
