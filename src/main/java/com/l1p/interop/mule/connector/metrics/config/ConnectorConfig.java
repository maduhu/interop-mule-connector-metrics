/**
 * Copyright (C) 2016, W Modus LLC. All rights reserved.
 */
package com.l1p.interop.mule.connector.metrics.config;

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

}