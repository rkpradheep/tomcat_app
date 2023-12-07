package com.server.chat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import com.server.common.Util;

@ServerEndpoint(value = "/chat")
public class ChatWebSocket
{
	private static final Map<Session, String> activeSessions = new ConcurrentHashMap<>();
	private static final Map<String, String> sessionIdVsFileName = new ConcurrentHashMap<>();
	private static final Map<String, ByteBuffer> sessionIdVsChunkedData = new ConcurrentHashMap<>();
	private static final Logger LOGGER = Logger.getLogger(ChatWebSocket.class.getName());

	@OnOpen
	public void OnOpen(Session session) throws IOException
	{
		if(activeSessions.containsValue(session.getRequestParameterMap().get("name").get(0)))
		{
			session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, "Duplicate name"));
			return;
		}
		session.setMaxBinaryMessageBufferSize(1024 * 300);
		session.setMaxTextMessageBufferSize(1024 * 300);
		activeSessions.put(session, session.getRequestParameterMap().get("name").get(0));
		LOGGER.info("New Session connected with id " + session.getId());

		if(Boolean.parseBoolean(session.getRequestParameterMap().get("rejoin").get(0)))
		{
			session.getBasicRemote().sendText("rejoined");
			return;
		}
		activeSessions.forEach((key, value) -> {
			try
			{
				if(key.isOpen())
				{
					if(key.equals(session))
						key.getBasicRemote().sendText("<b style='color:green;margin: 150px'>You Joined!</b></br></br>");
					else
						key.getBasicRemote().sendText("<b style='color:green;margin:150px'>" + activeSessions.get(session) + " Joined!</b></br></br>");
				}
				else
					activeSessions.remove(key);
			}
			catch(IOException e)
			{
				throw new RuntimeException(e);
			}
		});
	}

	@OnClose
	public void onClose(Session session, CloseReason closeReason)
	{
		LOGGER.info("Session closed " + session.getId());
		String name = activeSessions.remove(session);

		if(closeReason.getCloseCode() == CloseReason.CloseCodes.CLOSED_ABNORMALLY)
		{
			return;
		}

		activeSessions.forEach((key, value) -> {
			try
			{
				if(key.isOpen() && name != null)
				{
					key.getBasicRemote().sendText("<b style='color:red;margin: 150px'>" + name + " Left</b></br></br>");
				}
				else
					activeSessions.remove(key);
			}
			catch(IOException e)
			{
				throw new RuntimeException(e);
			}
		});
	}

	@OnMessage
	public void incoming(Session session, String msg) throws IOException
	{

		if(msg.contains("filename="))
		{
			Matcher matcher = Pattern.compile("filename=(.*)&size=(.*)").matcher(msg);

			matcher.matches();
			sessionIdVsFileName.put(session.getId(), matcher.group(1));
			ByteBuffer byteBuffer = ByteBuffer.allocate(Integer.parseInt(matcher.group(2)));
			sessionIdVsChunkedData.put(session.getId(), byteBuffer);
			return;
		}
		if(msg.contains("endoffile123"))
		{
			writeFile(session);
			return;
		}

		if(msg.contains("cancelCurrentFile"))
		{
			String fileName = sessionIdVsFileName.get(session.getId());
			sessionIdVsFileName.remove(session.getId());
			sessionIdVsChunkedData.remove(session.getId());
			session.getBasicRemote().sendText("currentFileCancelled");
			LOGGER.info("File " + fileName+  " cancelled successfully");
			return;
		}

		activeSessions.forEach((key, value) -> {
			try
			{
				if(key.isOpen())
				{
					if(key.equals(session))
						key.getBasicRemote().sendText(getFormattedMessage("You", msg));
					else
						key.getBasicRemote().sendText(getFormattedMessage(activeSessions.get(session), msg));
				}
				else
					activeSessions.remove(key);
			}
			catch(IOException e)
			{
				throw new RuntimeException(e);
			}
		});
	}

	@OnMessage
	public void handleFile(ByteBuffer data, Session session) throws IOException
	{
		String fileName = sessionIdVsFileName.get(session.getId());
		ByteBuffer byteBuffer = sessionIdVsChunkedData.get(session.getId());
		if(byteBuffer == null)
		{
			return;
		}
		byteBuffer.put(data.array());
		session.getBasicRemote().sendText("Uploading " + fileName + " [ " + (int) Math.floor(((double) byteBuffer.position() / byteBuffer.capacity()) * 100) + " % completed ]");
	}

	private void writeFile(Session session) throws IOException
	{
		ByteBuffer byteBuffer = sessionIdVsChunkedData.get(session.getId());
		new File(Util.HOME_PATH + "/TomcatBuild/webapps/ROOT/uploads").mkdirs();
		FileOutputStream fileOutputStream = new FileOutputStream(Util.HOME_PATH + "/TomcatBuild/webapps/ROOT/uploads/" + sessionIdVsFileName.get(session.getId()));
		fileOutputStream.write(byteBuffer.array());
		fileOutputStream.close();
		String msg = "<a  target='_blank' href='/uploads/" + sessionIdVsFileName.get(session.getId()) + "'>" + sessionIdVsFileName.get(session.getId()) + "</a></br></br>";
		sessionIdVsFileName.remove(session.getId());
		sessionIdVsChunkedData.remove(session.getId());
		activeSessions.forEach((key, value) -> {
			try
			{
				if(key.isOpen())
				{
					if(key.equals(session))
						key.getBasicRemote().sendText(getFormattedMessage("You", msg));
					else
						key.getBasicRemote().sendText(getFormattedMessage(activeSessions.get(session), msg));
				}
				else
					activeSessions.remove(key);
			}
			catch(IOException e)
			{
				throw new RuntimeException(e);
			}
		});
		session.getBasicRemote().sendText("fileuploaddone123");
	}

	private static String getFormattedMessage(String name, String message)
	{
		return name.equalsIgnoreCase("you") ? "<div class='container1 darker1' style='margin: 10px 250'><b class='user-name'>" + name + "</b> <br><br><p style='margin-left:15px'>" + message + "</p><span class='time-left'>" + Util.getFormattedCurrentTime() + "</span></div><br><br>"
			: "<div class='container darker' style='margin: 10px 0px'><b class='user-name'>" + name + "</b> <br><br><p style='margin-left:15px'>" + message + "</p><span class='time-left'>" + Util.getFormattedCurrentTime() + "</span></div><br><br>";
	}

	@OnError
	public void onError(Session session, Throwable throwable)
	{
		LOGGER.log(Level.SEVERE, "Error occurred for session " + session.getId(), throwable);
	}

}
