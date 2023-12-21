package com.server.stream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.websocket.OnClose;

import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint(value = "/api/v1/live")
public class Subscribers
{

	public String sessionID;
	public static Map<String, Session> sessionMap = new LinkedHashMap<>();
	public static List<String> newJoineeList = new ArrayList();

	@OnOpen
	public void OnOpen(Session session)
	{
		sessionID = session.getId();
		sessionMap.put(session.getId(), session);
		newJoineeList.add(session.getId());
		StreamServlet.restartBroadCast = true;


		System.out.println("Subscriber Session Started " + session.getId());
	}

	@OnClose
	public void onClose(Session ss)
	{
		try
		{
			sessionMap.remove(sessionID);
			ss.close();
			System.out.println("Subscriber Session closed " + sessionID);
		}
		catch(IOException e)
		{
		}
	}

}