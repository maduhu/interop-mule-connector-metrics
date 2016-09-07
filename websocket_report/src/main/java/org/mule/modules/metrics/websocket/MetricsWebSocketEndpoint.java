package org.mule.modules.metrics.websocket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.mule.modules.metrics.model.MetricsPortlet;
import org.mule.modules.metrics.model.MetricsValue;
import org.mule.modules.metrics.model.MetricsValueGeneral;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServerEndpoint(value = "/websockets", configurator = MetricsWebSocketConfig.class)
public class MetricsWebSocketEndpoint {

	private static Logger log = LoggerFactory
			.getLogger(MetricsWebSocketEndpoint.class);

	private static Queue<Session> queue = new ConcurrentLinkedQueue<Session>();

	private MetricsWebSocketHandler sessionHandler;

	@OnOpen
	public void onOpen(Session session) throws IOException {

		log.info("Session Open: " + session.getId());

		queue.add(session);

		if (sessionHandler == null) {
			sessionHandler = new MetricsWebSocketHandler();
		}
		try {
			sessionHandler.addSession(session);
		} catch (JsonParseException e) {
			throw new IOException(e.getMessage(), e);
		} catch (JsonMappingException e) {
			throw new IOException(e.getMessage(), e);
		} catch (IOException e) {
			throw new IOException(e.getMessage(), e);
		}
	}

	@OnMessage
	public String onMessage(String message, Session session) throws IOException {

		// throws IOException
		log.info("Message received: " + message);

		ObjectMapper mapper = new ObjectMapper();

		MetricsPortlet portlet;
		try {
			portlet = mapper.readValue(message, MetricsPortlet.class);
		} catch (JsonParseException e) {
			throw new IOException(e.getMessage(), e);
		} catch (JsonMappingException e) {
			throw new IOException(e.getMessage(), e);
		} catch (IOException e) {
			throw new IOException(e.getMessage(), e);
		}

		if ("add".equals(portlet.getAction())) {

			if (!sessionHandler.existPortlet(portlet)) {
				portlet.setId("");
				portlet.setName("");
				portlet.setType("General");
				portlet.setValue(new MetricsValueGeneral(portlet.getCategory(),
						System.currentTimeMillis() / 1000L));
				sessionHandler.addPortlet(portlet);

				MetricsPortlet answer = new MetricsPortlet();
				answer.setId(portlet.getId());
				answer.setAction("added");
				answer.setType("General");

				message = mapper.writeValueAsString(answer);
			} else {
				MetricsPortlet answer = new MetricsPortlet();
				answer.setId(portlet.getId());
				answer.setAction("exists");
				answer.setType("General");

				message = mapper.writeValueAsString(answer);

			}

		}

		if ("remove".equals(portlet.getAction())) {

			String id = portlet.getId();

			sessionHandler.removePortlet(id);

			MetricsPortlet answer = new MetricsPortlet();
			answer.setId(portlet.getId());
			answer.setAction("removed");

			message = mapper.writeValueAsString(answer);

		}

		if ("updateAll".equals(portlet.getAction())) {

			// This must be do it when we work with external websocket
			// server
		}

		if ("start".equals(portlet.getAction())) {

			session.getBasicRemote().sendText("Connected..");

		}

		return message;
	}

	@OnClose
	public void onClose(Session session, CloseReason closeReason) {
		sessionHandler.removeSession(session);
		queue.remove(session);
	}

	@OnError
	public void onError(Throwable error) {
		log.error("Web metric report error", error);
	}

	public void sendToAll(MetricsValue value) throws IOException {

		ArrayList<Session> closedSessions = new ArrayList<Session>();

		for (Session session : queue) {

			if (!session.isOpen()) {

				closedSessions.add(session);

			} else {

				MetricsPortlet portlet = new MetricsPortlet();

				portlet.setCategoryAndName(value.getName_id());

				portlet.setId(portlet.generateId());

				portlet.setType(value.getName_id());

				portlet.setValue(value);

				sessionHandler.updatePortlet(portlet);
			}
		}
		queue.removeAll(closedSessions);
	}

}
