package org.mule.modules.metrics.reporter;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.regex.Pattern;

import javax.net.SocketFactory;
import javax.websocket.DeploymentException;
import javax.websocket.Session;

import org.glassfish.tyrus.client.ClientManager;
import org.mule.modules.metrics.model.MetricsValue;
import org.mule.modules.metrics.websocket.MetricsWebSocketClient;
import org.mule.modules.metrics.websocket.MetricsWebSocketConfig;
import org.mule.modules.metrics.websocket.MetricsWebSocketEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A client to a Carbon server via TCP.
 */
public class WebSocket implements WebSocketSender {

	private static Logger log = LoggerFactory.getLogger(WebSocket.class);

	private static final Pattern WHITESPACE = Pattern.compile("[\\s]+");
	private static final Charset UTF_8 = Charset.forName("UTF-8");

	private final String hostname;
	private final int port;
	private final String servicename;
	private final Charset charset;
	private final boolean serverEmbedded;

	private Session session;
	private MetricsWebSocketEndpoint endPoint;

	/**
	 * Creates a new client which connects to the given address using the
	 * default {@link SocketFactory}.
	 *
	 * @param hostname
	 *            The hostname of the WebSocket server
	 * @param port
	 *            The port of the WebSocket server
	 * @param servicename
	 *            The service name of the WebSocket server
	 */
	public WebSocket(String hostname, int port, String servicename,
			boolean serverEmbedded) {
		this(hostname, port, servicename, serverEmbedded, UTF_8);
	}

	/**
	 * Creates a new client which connects to the given address and socket
	 * factory using the given character set.
	 *
	 * @param hostname
	 *            The hostname of the WebSocket server
	 * @param port
	 *            The port of the WebSocket server
	 * @param servicename
	 *            The service name of the WebSocket server
	 * @param charset
	 *            the character set used by the server
	 */
	public WebSocket(String hostname, int port, String servicename,
			boolean serverEmbedded, Charset charset) {
		this.hostname = hostname;
		this.port = port;
		this.servicename = servicename;
		this.charset = charset;
		this.serverEmbedded = serverEmbedded;
	}

	@Override
	public void connect() throws IllegalStateException, IOException {

		if (isConnected()) {
			throw new IllegalStateException("Already connected");
		}

		if (serverEmbedded) {

			endPoint = MetricsWebSocketConfig.webSocketEndpoint;
		} else {

			String uristr = "ws://" + this.hostname + ":"
					+ Integer.toString(this.port) + "/" + this.servicename
					+ "/websockets" + "/";

			log.info("Connecting to :" + uristr);

			ClientManager client = ClientManager.createClient();

			try {
				session = client.connectToServer(MetricsWebSocketClient.class,
						new URI(uristr));

				log.info("Id :" + session.getId());

			} catch (DeploymentException e) {
				log.error("Error Connecting to :" + uristr, e);
				throw new IOException(e);
			} catch (URISyntaxException e) {
				log.error("Error Connecting to :" + uristr, e);
				throw new IOException(e);
			}

		}

	}

	@Override
	public boolean isConnected() {

		if (serverEmbedded) {
			return endPoint != null;
		} else {
			return session != null;
		}

	}

	@Override
	public void send(MetricsValue value) throws IOException {

		if (serverEmbedded) {

			if (MetricsWebSocketConfig.webSocketEndpoint != null) {

				MetricsWebSocketConfig.webSocketEndpoint.sendToAll(value);

				log.info("Message sent: " + value.toString());

			}

		} else {

			if (session != null) {

				// JsonProvider provider = JsonProvider.provider();
				//
				// javax.json.JsonObject answerMessage = provider
				// .createObjectBuilder().add("action", "updateAll")
				// .add("name", name).add("value", value).build();
				//
				// String message = answerMessage.toString();
				//
				// log.info("Sending message: " + message);
				//
				// try {
				//
				// session.getBasicRemote().sendText(message);
				//
				// log.info("Message sent: " + message);
				//
				// } catch (IOException e) {
				// log.error("Error sending message", e);
				// throw new IOException(e);
				// }

			}
		}

	}

	@Override
	public int getFailures() {
		return 0;
	}

	@Override
	public void flush() throws IOException {

	}

	@Override
	public void close() throws IOException {

		try {
			session.close();
		} catch (Exception e) {
			log.error("Error clossing session", e);
			throw new IOException(e);
		}

	}

	protected String sanitize(String s) {
		return WHITESPACE.matcher(s).replaceAll("-");
	}

}