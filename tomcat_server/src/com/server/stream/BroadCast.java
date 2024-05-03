package com.server.stream;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import com.server.framework.common.Util;

@ServerEndpoint(value = "/api/v1/broadcast")
public class BroadCast
{

	@OnOpen
	public void OnOpen(Session session)
	{
		System.out.println("Broadcat Session Started " + session.getId());
	}

	@OnMessage
	public void broadcast(Session session, ByteBuffer bb, boolean last)
	{
		try
		{
			for(Session sessions : Subscribers.sessionMap.values())
				if(session.isOpen())
					sessions.getBasicRemote().sendBinary(bb, last);
		}
		catch(Exception e)
		{
			System.out.println(e);
		}
	}

	@OnClose
	public void onClose(Session ss)
	{
		try
		{
			System.out.println("Broadcast Session Closed " + ss.getId());
			ss.close();
		}
		catch(IOException e)
		{
		}
	}

	@OnMessage
	public void sendText(Session session, String msg, boolean last)
	{
		try
		{
			if(session.isOpen())
			{
				session.getBasicRemote().sendText("Hello " + msg, last);
			}
		}
		catch(IOException e)
		{
		}
	}

	public static void tel()
	{
		try
		{
			FileInputStream fileInputStream = new FileInputStream(Util.HOME_PATH + "/Downloads/ct1-datamigration.mp4");
			ByteBuffer byteBuffer = ByteBuffer.allocate((int) fileInputStream.getChannel().size());
			byte[] b = new byte[1024];
			int i = fileInputStream.getChannel().read(byteBuffer);
			fileInputStream.close();

			for(Session sessions : Subscribers.sessionMap.values())
				if(sessions.isOpen())
					sessions.getBasicRemote().sendBinary(byteBuffer);
		}
		catch(Exception e)
		{
			System.out.println(e);
		}
	}

}