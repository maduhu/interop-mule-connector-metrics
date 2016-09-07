/**
 * Copyright (C) 2016, W Modus LLC. All rights reserved.
 */
package org.mule.modules.metrics.config;

import java.util.Collections;
import java.util.List;

import org.mule.api.annotations.Configurable;
import org.mule.api.annotations.components.Configuration;
import org.mule.api.annotations.display.FriendlyName;
import org.mule.api.annotations.display.Placement;
import org.mule.api.annotations.display.Summary;
import org.mule.api.annotations.param.Default;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;

/**
 * Metrics connector configuration...
 *
 * @author Honain Khan
 * @author Modusbox, Inc.
 */
@Configuration(friendlyName = "Metric Connector Configuration")
public class ConnectorConfig {

	@Configurable
	private MetricRegistry metricRegistry;

	@Configurable
	private List<ScheduledReporter> scheduledReporters = Collections
			.emptyList();

	@Configurable
	private int reporterInterval;

	@Configurable
	@Summary("Enter Host")
	@FriendlyName("Host")
	@Placement(tab = "WebSocket Server", group = "WebSocket Server", order = 1)
	private String host;

	@Configurable
	@Summary("Enter Port")
	@FriendlyName("Port")
	@Placement(tab = "WebSocket Server", group = "WebSocket Server", order = 2)
	private int port;

	@Configurable
	@Summary("Enter Service Name")
	@FriendlyName("Service Name")
	@Placement(tab = "WebSocket Server", group = "WebSocket Server", order = 3)
	private String servicename;

	@Configurable
	@Summary("Use WebSocket Server Embedded")
	@FriendlyName("Use WebSocket Server Embedded")
	@Default("false")
	@Placement(tab = "WebSocket Server", group = "WebSocket Server", order = 4)
	private boolean serverembedded;

	public MetricRegistry getMetricRegistry() {
		return metricRegistry;
	}

	public void setMetricRegistry(MetricRegistry metricRegistry) {
		this.metricRegistry = metricRegistry;
	}

	public int getReporterInterval() {
		return reporterInterval;
	}

	public void setReporterInterval(int reporterInterval) {
		this.reporterInterval = reporterInterval;
	}

	public List<ScheduledReporter> getScheduledReporters() {
		return scheduledReporters;
	}

	public void setScheduledReporters(List<ScheduledReporter> scheduledReporters) {
		this.scheduledReporters = scheduledReporters;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getServicename() {
		return servicename;
	}

	public void setServicename(String servicename) {
		this.servicename = servicename;
	}

	public boolean isServerembedded() {
		return serverembedded;
	}

	public void setServerembedded(boolean serverembedded) {
		this.serverembedded = serverembedded;
	}

}