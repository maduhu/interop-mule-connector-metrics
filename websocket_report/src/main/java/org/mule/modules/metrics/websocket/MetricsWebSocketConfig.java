package org.mule.modules.metrics.websocket;

import javax.websocket.server.ServerEndpointConfig.Configurator;

public class MetricsWebSocketConfig extends Configurator {

	public static final MetricsWebSocketEndpoint webSocketEndpoint = new MetricsWebSocketEndpoint();

	public <T> T getEndpointInstance(Class<T> endpointClass)
			throws InstantiationException {

		if (endpointClass.equals(MetricsWebSocketEndpoint.class)) {
			return (T) webSocketEndpoint;
		}

		throw new InstantiationException();

	}

}
