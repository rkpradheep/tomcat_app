package com.server.snakegame;

import jakarta.websocket.CloseReason;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

@ServerEndpoint(value = "/api/v1/snakegame")
public class SnakeGameHandler
{

	private static final Logger LOGGER = Logger.getLogger(SnakeGameHandler.class.getName());

	@OnOpen
	public void OnOpen(Session session) throws IOException
	{
		List<String> paramList = session.getRequestParameterMap().get("game_code");
		if(Objects.isNull(paramList) || paramList.isEmpty() || StringUtils.isEmpty(paramList.get(0)))
		{
			SnakeGameUtil.handleNewGame(session);
		}
		else
		{
			SnakeGameUtil.handleJoinGame(paramList.get(0), session);
		}
	}

	@OnClose
	public void onClose(Session session, CloseReason closeReason) throws IOException
	{
		SnakeGameUtil.handlePayerRemoval(session);
	}

	@OnMessage
	public void incoming(Session session, String msg) throws IOException
	{
		JSONObject data = new JSONObject(msg);

		if(StringUtils.equals("keydown", data.getString("command")))
		{
			SnakeGameUtil.handleKeyDown(data.getJSONObject("data").getInt("key_code"), session);
		}
	}

	@OnError
	public void onError(Session session, Throwable throwable)
	{
		LOGGER.log(Level.SEVERE, "Error occurred for session " + session.getId(), throwable);
	}

}
