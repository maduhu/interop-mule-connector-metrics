package org.mule.modules.metrics.websocket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;

import org.codehaus.jackson.map.ObjectMapper;
import org.mule.modules.metrics.model.MetricsPortlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ClientEndpoint
public class MetricsWebSocketClient {

	private static Logger log = LoggerFactory
			.getLogger(MetricsWebSocketClient.class);

	@OnOpen
	public void onOpen(Session session) {

		log.info("Connected ... " + session.getId());

		try {

			MetricsPortlet portlet = new MetricsPortlet();

			portlet.setAction("start");

			ObjectMapper mapper = new ObjectMapper();

			String jsonString = mapper.writeValueAsString(portlet);

			session.getBasicRemote().sendText(jsonString);

		} catch (IOException e) {

			throw new RuntimeException(e);

		}
	}

	@OnMessage
	public String onMessage(String message, Session session) {
		BufferedReader bufferRead = new BufferedReader(new InputStreamReader(
				System.in));
		try {
			log.info("Received ...." + message);
			String userInput = bufferRead.readLine();
			return userInput;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@OnClose
	public void onClose(Session session, CloseReason closeReason) {
		log.info(String.format("Session %s close because of %s",
				session.getId(), closeReason));
	}

}
