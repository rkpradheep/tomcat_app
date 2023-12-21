package com.server.webrtc;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.json.JSONObject;

import com.server.common.Util;

@ServerEndpoint("/signal")
public class WebRTCSignallingServer
{
	private static final ConcurrentMap<String, OffererAnswererMeta> offererAnswererMetaMap = new ConcurrentHashMap<>();
	private static final Logger LOGGER = Logger.getLogger(WebRTCSignallingServer.class.getName());

	@OnOpen
	public void onOpen(Session session) throws IOException
	{
		LOGGER.info("Session connected " + session.getId());
		if(isAnswerer(session))
		{
			String id = session.getRequestParameterMap().get("id").get(0);

			if(!offererAnswererMetaMap.containsKey(id))
			{
				session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, "InvalidCallId"));
				return;
			}

			if(offererAnswererMetaMap.get(id).getAnswererSession() != null)
			{
				session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, "Already one peer connected"));
				return;
			}

			offererAnswererMetaMap.get(id).setAnswererSession(session);

		}
		else
		{
			OffererAnswererMeta offererAnswererMeta = new OffererAnswererMeta();
			offererAnswererMeta.setOffererSession(session);
			offererAnswererMetaMap.put(session.getId(), offererAnswererMeta);

			JSONObject ack = new JSONObject();
			ack.put("type", "ACK");
			ack.put("id", session.getId());
			session.getBasicRemote().sendText(ack.toString());
		}

		session.setMaxTextMessageBufferSize(1024 * 300);
	}

	@OnMessage
	public void onMessage(Session session, String message) throws IOException
	{
		LOGGER.info("Session message " + message);

		JSONObject jsonObject = new JSONObject(message);

		String sessionID = jsonObject.getString("id");

		if(jsonObject.getString("type").equals("mail"))
		{
			String mailContent = "<b>" + jsonObject.getString("invitorname") + "</b> is asking you to join the <a href='https://" + session.getRequestURI().getHost() + "/webrtc.html'>call</a>. Please join using below Session ID<br><br><b>Session ID : </b> " + sessionID;
			Util.sendEmail("Meeting Invitation", jsonObject.getString("mailid"), mailContent);
		}
		else if(jsonObject.getString("type").equals("stream_closed"))
		{
			if(offererAnswererMetaMap.get(sessionID).getOffererSession().equals(session))
			{
				if(offererAnswererMetaMap.get(sessionID).getAnswererSession() != null)
					offererAnswererMetaMap.get(sessionID).getAnswererSession().getBasicRemote().sendText(jsonObject.toString());
			}
			else
			{
				offererAnswererMetaMap.get(sessionID).getOffererSession().getBasicRemote().sendText(jsonObject.toString());
			}
		}
		else if(jsonObject.getString("type").equals("need_offer"))
		{

			JSONObject joinedData = new JSONObject();
			joinedData.put("type", "joined");
			joinedData.put("message", session.getRequestParameterMap().get("name").get(0) + " joined the call");
			offererAnswererMetaMap.get(sessionID).getOffererSession().getBasicRemote().sendText(joinedData.toString());

			if(offererAnswererMetaMap.get(sessionID).getOffer() != null)
			{
				JSONObject answer = new JSONObject();
				answer.put("type", "offer");
				answer.put("offer", offererAnswererMetaMap.get(sessionID).getOffer());

				session.getBasicRemote().sendText(answer.toString());
			}
		}
		else if(jsonObject.getString("type").equals("offer"))
		{
			if(jsonObject.optBoolean("is_switch_role"))
			{
				switchRole(sessionID, session);
			}

			offererAnswererMetaMap.get(sessionID).setOffer(jsonObject.getJSONObject("offer"));

			offererAnswererMetaMap.get(sessionID).getOffererCandidateList().clear();

			if(offererAnswererMetaMap.get(sessionID).getAnswererSession() != null)
			{
				offererAnswererMetaMap.get(sessionID).getAnswererSession().getBasicRemote().sendText(jsonObject.toString());
			}
		}

		else if(jsonObject.getString("type").equals("answer"))
		{
			offererAnswererMetaMap.get(sessionID).getOffererSession().getBasicRemote().sendText(jsonObject.toString());
			publishOfferCandidate(sessionID);
		}

		else if(jsonObject.getString("type").equals("candidate"))
		{
			if(jsonObject.getBoolean("is_offerer"))
			{
				offererAnswererMetaMap.get(sessionID).addToOffererCandidateList(jsonObject.getJSONObject("candidate"));
				publishOfferCandidate(sessionID);
			}
			else
			{
				offererAnswererMetaMap.get(sessionID).getOffererSession().getBasicRemote().sendText(jsonObject.toString());
			}
		}

	}

	@OnClose
	public void onClose(Session session, CloseReason closeReason) throws IOException
	{
		LOGGER.log(Level.SEVERE, "Session Closed " + closeReason.getReasonPhrase());

		if(offererAnswererMetaMap.containsKey(session.getId()))
		{
			Session sessionToBeIntimated = null;
			if(offererAnswererMetaMap.get(session.getId()).getAnswererSession() != null)
			{
				if(offererAnswererMetaMap.get(session.getId()).getAnswererSession().getId().equals(session.getId()))
					sessionToBeIntimated = offererAnswererMetaMap.get(session.getId()).getOffererSession();
				else
					sessionToBeIntimated = offererAnswererMetaMap.get(session.getId()).getAnswererSession();

				sessionToBeIntimated.close(new CloseReason(CloseReason.CloseCodes.GOING_AWAY, "PeerClosed"));
			}

			offererAnswererMetaMap.remove(session.getId());
		}
		else
		{

			for(Map.Entry<String, OffererAnswererMeta> sessionMetaEntrySet : offererAnswererMetaMap.entrySet())
			{
				if(sessionMetaEntrySet.getValue().getOffererSession().getId().equals(session.getId()))
				{
					offererAnswererMetaMap.get(sessionMetaEntrySet.getKey()).getAnswererSession().close(new CloseReason(CloseReason.CloseCodes.GOING_AWAY, "PeerClosed"));
				}
				else if(sessionMetaEntrySet.getValue().getAnswererSession().getId().equals(session.getId()))
				{
					offererAnswererMetaMap.get(sessionMetaEntrySet.getKey()).getOffererSession().close(new CloseReason(CloseReason.CloseCodes.GOING_AWAY, "PeerClosed"));
				}
			}

		}
	}

	private boolean isAnswerer(Session session)
	{
		return session.getRequestParameterMap().containsKey("id");
	}

	private void switchRole(String sessionID, Session session)
	{
		OffererAnswererMeta offererAnswererMeta = offererAnswererMetaMap.get(sessionID);

		Session offererSession = offererAnswererMeta.getOffererSession();
		offererAnswererMeta.setOffererSession(session);
		offererAnswererMeta.setAnswererSession(offererSession);

		offererAnswererMeta.getOffererCandidateList().clear();

	}

	private void publishOfferCandidate(String sessionID)
	{
		Session answererSession = offererAnswererMetaMap.get(sessionID).getAnswererSession();

		if(answererSession == null)
		{
			return;
		}
		offererAnswererMetaMap.get(sessionID).getOffererCandidateList().stream().forEach(offerCandidate -> {
			JSONObject candidate = new JSONObject();
			candidate.put("type", "candidate");
			candidate.put("candidate", offerCandidate);
			try
			{
				answererSession.getBasicRemote().sendText(candidate.toString());
			}
			catch(IOException e)
			{
				throw new RuntimeException(e);
			}
		});
		offererAnswererMetaMap.get(sessionID).getOffererCandidateList().clear();
	}
}
