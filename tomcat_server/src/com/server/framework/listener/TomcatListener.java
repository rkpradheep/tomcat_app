package com.server.framework.listener;

import java.net.ProxySelector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;

import com.server.framework.common.Configuration;
import com.server.framework.common.FileManager;
import com.server.framework.common.ProxySelectorExtension;
import com.server.framework.common.ProxyServer;
import com.server.framework.job.RefreshManager;
import com.server.framework.security.DBUtil;
import com.server.framework.security.SecurityUtil;

public class TomcatListener implements LifecycleListener
{
	private static final Logger LOGGER = Logger.getLogger(TomcatListener.class.getName());

	@Override
	public void lifecycleEvent(LifecycleEvent event) {
		if(Lifecycle.AFTER_START_EVENT.equals(event.getType())){

			LOGGER.log(Level.INFO, "Initializing Context for {0} at {1}", new Object[] {SecurityUtil.getFormattedCurrentTime()});

			Configuration.load();

			DBUtil.initialiseDataSource();

			RefreshManager.init();

			ProxySelector.setDefault(new ProxySelectorExtension());

			FileManager.copyUploads();

			if(Configuration.getBoolean("proxy.server"))
			{
				ProxyServer.init();
			}

			LOGGER.log(Level.INFO, "Context Initialised for at {0}", new Object[] {SecurityUtil.getFormattedCurrentTime()});
		}
		if(Lifecycle.AFTER_DESTROY_EVENT.equals(event.getType())){

			DBUtil.closeDataSource();

			RefreshManager.shutDown();

			if(Configuration.getBoolean("proxy.server"))
			{
				ProxyServer.shutDown();
			}

			LOGGER.log(Level.INFO, "Context Destroyed at {1}", new Object[] {SecurityUtil.getFormattedCurrentTime()});
		}
	}
}