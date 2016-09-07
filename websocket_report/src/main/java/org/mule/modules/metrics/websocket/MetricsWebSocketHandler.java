package org.mule.modules.metrics.websocket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.websocket.Session;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.mule.modules.metrics.model.MetricsPortlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetricsWebSocketHandler {

	private static Logger log = LoggerFactory
			.getLogger(MetricsWebSocketHandler.class);

	private int portletId = 0;
	private final Set<Session> sessions = new HashSet<>();
	private final Set<MetricsPortlet> portlets = new HashSet<>();

	public MetricsWebSocketHandler() {

	}

	public void addSession(Session session) throws JsonGenerationException,
			JsonMappingException, IOException {

		sessions.add(session);

		for (MetricsPortlet portlet : portlets) {
			sendToSession(session, portlet);
		}

	}

	public void removeSession(Session session) {
		sessions.remove(session);
	}

	public List<MetricsPortlet> getDevices() {
		return new ArrayList<>(portlets);
	}

	public void addPortlet(MetricsPortlet portlet)
			throws JsonGenerationException, JsonMappingException, IOException {

		portlet.setId(portlet.generateId(portlet.getCategory(),
				portlet.getName()));

		portlets.add(portlet);

		portlet.setAction("add");

		sendToAllConnectedSessions(portlet);

	}

	public boolean existPortlet(MetricsPortlet portlet) {

		boolean returnValue = false;

		for (MetricsPortlet portletV : portlets) {

			if (portletV.getId()
					.compareTo(
							portlet.generateId(portlet.getCategory(),
									portlet.getName())) == 0) {
				returnValue = true;
				break;

			}
		}

		return returnValue;

	}

	public void updatePortlet(MetricsPortlet portlet)
			throws JsonGenerationException, JsonMappingException, IOException {

		portlet.setAction("update");

		sendToAllConnectedSessions(portlet);

	}

	public void removePortlet(String id) throws JsonGenerationException,
			JsonMappingException, IOException {

		MetricsPortlet portlet = getDeviceById(id);

		if (portlet != null) {

			portlets.remove(portlet);

			MetricsPortlet answer = new MetricsPortlet();
			answer.setId(id);
			answer.setAction("remove");

			sendToAllConnectedSessions(answer);
		}

	}

	private MetricsPortlet getDeviceById(String id) {
		for (MetricsPortlet portlet : portlets) {
			if (portlet.getId().compareTo(id) == 0) {
				return portlet;
			}
		}
		return null;
	}

	private String createMessage(MetricsPortlet portlet)
			throws JsonGenerationException, JsonMappingException, IOException {

		ObjectMapper mapper = new ObjectMapper();

		portlet.setType(portlet.getValue().getType());

		String jsonString = mapper.writeValueAsString(portlet);

		return jsonString;
	}

	private void sendToAllConnectedSessions(MetricsPortlet portlet)
			throws JsonGenerationException, JsonMappingException, IOException {

		for (Session session : sessions) {
			sendToSession(session, portlet);
			log.info("Sending message to Session ID [" + session.getId()
					+ "]: [" + portlet.toString() + "]");
		}
	}

	private void sendToSession(Session session, MetricsPortlet portlet)
			throws JsonGenerationException, JsonMappingException, IOException {

		String message = createMessage(portlet);

		try {
			session.getBasicRemote().sendText(message);
		} catch (IOException error) {
			sessions.remove(session);
			log.error("Web metric report error", error);
		}
	}

}
