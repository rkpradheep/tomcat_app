package com.server.webrtc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.server.framework.common.Configuration;
import com.server.framework.security.SecurityUtil;

public class WebRTCUtil extends HttpServlet
{
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException
	{
		List<Map<String, String>> iceServers = new ArrayList<>();

		Map<String, String> iceServer = new HashMap<>();
		iceServer.put("urls", "stun:stun.relay.metered.ca:80");
		iceServers.add(iceServer);

		String userName = Configuration.getProperty("webrtc.iceserver.username");
		String credential = Configuration.getProperty("webrtc.iceserver.credential");

		for(String iceserverUrl : Configuration.getProperty("webrtc.icecserver.urls").split(","))
		{
			iceServer = new HashMap<>();
			iceServer.put("urls", iceserverUrl);
			iceServer.put("username", userName);
			iceServer.put("credential", credential);
			iceServers.add(iceServer);
		}

		SecurityUtil.writeJSONResponse(response, iceServers);
	}
}
