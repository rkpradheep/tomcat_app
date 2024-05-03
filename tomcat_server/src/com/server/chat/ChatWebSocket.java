package com.server.chat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Arrays;

import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import com.server.framework.common.Util;
import com.server.framework.security.SecurityUtil;

@ServerEndpoint(value = "/api/v1/chat")
public class ChatWebSocket
{
	private static final Map<Session, String> activeSessions = new ConcurrentHashMap<>();
	private static final Map<String, String> sessionIdVsFileName = new ConcurrentHashMap<>();
	private static final Map<String, ByteBuffer> sessionIdVsChunkedData = new ConcurrentHashMap<>();
	private static final Logger LOGGER = Logger.getLogger(ChatWebSocket.class.getName());

	private static final Consumer<String[]> handleMessage = (messageParams) -> {
		String messageForSender = messageParams[0], messageForReceiver = messageParams[1], senderName = messageParams[2];
		activeSessions.forEach((key, value) -> {
			try
			{
				if(value.equalsIgnoreCase(senderName))
				{
					ChatWebSocketUtil.addMessage(senderName, messageForSender);
				}

				if(key.isOpen())
				{
					if(value.equalsIgnoreCase(senderName))
					{
						key.getBasicRemote().sendText(messageForSender);
					}
					else
					{
						ChatWebSocketUtil.addMessage(activeSessions.get(key), messageForReceiver);
						key.getBasicRemote().sendText(messageForReceiver);
					}
				}
				else
				{
					activeSessions.remove(key);
				}
			}
			catch(IOException e)
			{
				LOGGER.log(Level.SEVERE, "Exception occurred", e);
			}
		});
	};

	@OnOpen
	public void OnOpen(Session session) throws IOException
	{
		for(String name : activeSessions.values())
		{
			if(name.trim().equalsIgnoreCase(session.getRequestParameterMap().get("name").get(0).trim()))
			{
				session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, "duplicate_name"));
				break;
			}
		}
		if(!session.isOpen())
		{
			return;
		}
		session.setMaxBinaryMessageBufferSize(1024 * 300);
		session.setMaxTextMessageBufferSize(1024 * 300);
		activeSessions.put(session, session.getRequestParameterMap().get("name").get(0));
		LOGGER.info("New Session connected with id " + session.getId());

		String name = session.getRequestParameterMap().get("name").get(0);
		ChatWebSocketUtil.addOrGetUser(name);

		if(Boolean.parseBoolean(session.getRequestParameterMap().get("rejoin").get(0)))
		{
			session.getBasicRemote().sendText("rejoined");
			return;
		}

		String messageForSender = ChatWebSocketUtil.getPreviousMessage(name) + "<b style='color:green;margin: 140px'>You Joined! [ " + SecurityUtil.getFormattedCurrentTime() + " ]</b></br></br>";
		String messageForReceiver = "<b style='color:green;margin:140px'>" + name + " Joined!</b></br></br>";

		handleMessage.accept(new String[] {messageForSender, messageForReceiver, name});
	}

	@OnClose
	public void onClose(Session session, CloseReason closeReason)
	{
		LOGGER.info("Session closed " + session.getId());
		String name = activeSessions.remove(session);
		if(Objects.isNull(name))
		{
			return;
		}
		String leftOrDisconnected = Arrays.asList(CloseReason.CloseCodes.CLOSED_ABNORMALLY, CloseReason.CloseCodes.GOING_AWAY).contains(closeReason.getCloseCode()) ? " Disconnected!" : " Left!";

		String messageForSender = "<b style='color:red;margin: 140px'>You" + leftOrDisconnected + " [ " + SecurityUtil.getFormattedCurrentTime() + " ]</b></br></br>";
		messageForSender = leftOrDisconnected.equals(" Disconnected!") ? messageForSender.replace("You", "") : messageForSender;
		String messageForReceiver = "<b style='color:red;margin: 140px'>" + name + leftOrDisconnected + "</b></br></br>";

		handleMessage.accept(new String[] {messageForSender, messageForReceiver, name});
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
			LOGGER.info("File " + fileName + " cancelled successfully");
			return;
		}

		handleMessage.accept(new String[] {getFormattedMessage("You", msg), getFormattedMessage(activeSessions.get(session), msg), activeSessions.get(session)});

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
		byte[] byteArray = byteBuffer.array();

		new File(Util.HOME_PATH + "/tomcat_build/webapps/ROOT/uploads").mkdirs();
		FileOutputStream fileOutputStream = new FileOutputStream(Util.HOME_PATH + "/tomcat_build/webapps/ROOT/uploads/" + sessionIdVsFileName.get(session.getId()));
		fileOutputStream.write(byteArray);
		fileOutputStream.close();

		new File(Util.HOME_PATH + "/uploads").mkdirs();
		fileOutputStream = new FileOutputStream(Util.HOME_PATH + "/uploads/" + sessionIdVsFileName.get(session.getId()));
		fileOutputStream.write(byteArray);
		fileOutputStream.close();

		//uploadFile(sessionIdVsFileName.get(session.getId()), new ByteArrayInputStream(byteBuffer.array()));

		String msg = "<a  target='_blank' href='/uploads/" + sessionIdVsFileName.get(session.getId()) + "'>" + sessionIdVsFileName.get(session.getId()) + "</a></br></br>";
		sessionIdVsFileName.remove(session.getId());
		sessionIdVsChunkedData.remove(session.getId());

		String messageForSender = getFormattedMessage("You", msg);
		String messageForReceiver = getFormattedMessage(activeSessions.get(session), msg);

		handleMessage.accept(new String[] {messageForSender, messageForReceiver, activeSessions.get(session)});

		session.getBasicRemote().sendText("fileuploaddone123");
	}

	private static String getFormattedMessage(String name, String message)
	{
		return name.equalsIgnoreCase("you") ? "<div class='container1 darker1' style='margin: 10px 250'><b class='user-name'>" + name + "</b> <br><br><p style='margin-left:15px'>" + message + "</p><span class='time-left'>" + SecurityUtil.getFormattedCurrentTime() + "</span></div><br><br>"
			: "<div class='container darker' style='margin: 10px 0px'><b class='user-name'>" + name + "</b> <br><br><p style='margin-left:15px'>" + message + "</p><span class='time-left'>" + SecurityUtil.getFormattedCurrentTime() + "</span></div><br><br>";
	}

	@OnError
	public void onError(Session session, Throwable throwable)
	{
		LOGGER.log(Level.SEVERE, "Error occurred for session " + session.getId(), throwable);
	}

}
