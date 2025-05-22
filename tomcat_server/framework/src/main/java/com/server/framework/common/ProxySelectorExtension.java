package com.server.framework.common;

import java.io.IOException;
import java.net.Proxy;
import java.net.SocketAddress;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProxySelectorExtension extends java.net.ProxySelector
{
	private static final Logger LOGGER = Logger.getLogger(ProxySelectorExtension.class.getName());

	@Override public List<Proxy> select(URI uri)
	{
		//LOGGER.log(Level.INFO, "Internal API call made for URI {0}", uri.getScheme() + "://" + uri.getHost() + getPort(uri) + uri.getPath());
		return Collections.singletonList(Proxy.NO_PROXY);
	}

	@Override public void connectFailed(URI uri, SocketAddress sa, IOException ioe)
	{
		LOGGER.log(Level.SEVERE, "Connection failed for URI " + uri, ioe);
	}

	private static String getPort(URI uri)
	{
		return uri.getPort() == -1 ? "" : ":" + uri.getPort();
	}
}
