package com.server.framework.listener;

import java.util.logging.Logger;

import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;

public class TomcatListener implements LifecycleListener
{
	private static final Logger LOGGER = Logger.getLogger(TomcatListener.class.getName());

	@Override
	public void lifecycleEvent(LifecycleEvent event) {
		if(Lifecycle.BEFORE_START_EVENT.equals(event.getType())){

//			LOGGER.log(Level.INFO, "Initializing Context started");
//
//			Configuration.load();
//
//			DBUtil.initialiseDataSource();
//
//			RefreshManager.init();
//
//			//ProxySelector.setDefault(new ProxySelectorExtension());
//
//			FileManager.copyUploads();
//
//			if(Configuration.getBoolean("proxy.server"))
//			{
//				ProxyServer.init(Configuration.getProperty("proxy.port"), Configuration.getProperty("proxy.user"),  Configuration.getProperty("proxy.password"));
//			}
//
//			LOGGER.log(Level.INFO, "Context Initialised for at {0}", new Object[] {DateUtil.getFormattedCurrentTime()});
		}
		if(Lifecycle.BEFORE_DESTROY_EVENT.equals(event.getType())){

//			DBUtil.closeDataSource();
//
//			RefreshManager.shutDown();
//
//			if(Configuration.getBoolean("proxy.server"))
//			{
//				ProxyServer.shutDown();
//			}
//
//			LOGGER.log(Level.INFO, "Context Destroyed at {1}", new Object[] {DateUtil.getFormattedCurrentTime()});
		}
	}
}
