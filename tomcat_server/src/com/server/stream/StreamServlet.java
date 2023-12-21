package com.server.stream;

import java.io.IOException;
import java.nio.ByteBuffer;

import javax.servlet.http.HttpServlet;
import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint(value = "/api/v1/stream")
public class StreamServlet extends HttpServlet
{
	public static boolean broadCastStarted = false;
	public static boolean restartBroadCast = false;
	public static boolean restartSignalSent = false;

	@OnOpen
	public void OnOpen(Session session) throws IOException
	{
		if(broadCastStarted)
		{
			session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, "Broadcasting already"));
			return;
		}
		session.setMaxBinaryMessageBufferSize(1024 * 999);
		broadCastStarted = true;
	}
	@OnMessage
	public void broadcast(ByteBuffer bb, Session session)
	{
		try
		{
			if(restartBroadCast)
			{
				if(restartSignalSent)
				{
					return;
				}
				for(Session sessionJoined : Subscribers.sessionMap.values())
				{
					if(Subscribers.newJoineeList.contains(sessionJoined.getId()))
					{
						Subscribers.newJoineeList.remove(sessionJoined.getId());
						continue;
					}
					if(session.isOpen())
						sessionJoined.getBasicRemote().sendText("reset");
				}
				session.getBasicRemote().sendText("reset");
				restartSignalSent = true;
				return;
			}

			for(Session sessions : Subscribers.sessionMap.values())
				if(session.isOpen())
					sessions.getBasicRemote().sendBinary(bb);
		}
		catch(Throwable e)
		{
			System.out.println(e);
		}
	}

	@OnMessage
	public void messageFromBroadCaster(Session session, String message) throws IOException
	{
		System.out.println("Restarted");
		restartBroadCast = false;
		restartSignalSent = false;

		session.getBasicRemote().sendText("restartACK");
	}

	@OnError
	public void onError(Session session, Throwable throwable) {
		System.err.println("com.server.chat.WebSocket Error: " + throwable.getMessage());
		throwable.printStackTrace();
	}

	@OnClose
	public void onClose(Session session, CloseReason closeReason)
	{
		restartBroadCast = false;
		restartSignalSent = false;
		broadCastStarted = false;
		System.out.println("Closed " + closeReason.getCloseCode());
	}
}
