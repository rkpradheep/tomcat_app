package com.server.admin.logs;

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint(value = "/api/v1/admin/live/logs")
public class LiveLogHandler
{
	private static final Logger LOGGER = Logger.getLogger(LiveLogHandler.class.getName());

	private static final List<Session> liveSessionList = new ArrayList<>();

	@OnOpen
	public void OnOpen(Session session)
	{
		liveSessionList.add(session);
		LogWatchService.start();
		LOGGER.info("Session joined : " + session.getId());
	}

	public static void broadcast(String message)
	{
		try
		{
			for(Session session : liveSessionList)
			{
				if(session.isOpen())
				{
					session.getBasicRemote().sendText(message);
				}
				else
				{
					liveSessionList.remove(session);
				}
			}
		}
		catch(Exception e)
		{
			LOGGER.log(Level.SEVERE, "Exception occurred", e);
		}
	}

	@OnClose
	public void onClose(Session ss)
	{
		try
		{
			liveSessionList.remove(ss);
			LOGGER.info("Session Closed " + ss.getId());
			ss.close();
			if(liveSessionList.isEmpty())
			{
				LogWatchService.stop();
			}
		}
		catch(IOException e)
		{
			LOGGER.log(Level.SEVERE, "Exception occurred", e);
		}
	}

	@OnError
	public void onError(Session session, Throwable throwable)
	{
		LOGGER.log(Level.SEVERE, "Error occurred for session " + session.getId(), throwable);
	}

}
