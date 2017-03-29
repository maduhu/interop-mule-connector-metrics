package com.l1p.interop.mule.connector.metrics.unit;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mule.component.SimpleCallableJavaComponentTestCase;

import com.codahale.metrics.MetricRegistry;
import com.l1p.interop.mule.connector.metrics.MetricsConnector;
import com.l1p.interop.mule.connector.metrics.config.ConnectorConfig;

public class MetricConnectorTests extends SimpleCallableJavaComponentTestCase {
	
	MetricsConnector connector;
	ConnectorConfig config;

	
	@Test
	public void testConnectorConfigSetters() {
		connector = new MetricsConnector();
		
		MetricRegistry registry = new MetricRegistry();

		config = new ConnectorConfig();
		config.setMetricRegistry(registry);
		connector.setConfig(config);
		
		ConnectorConfig testConnector = connector.getConfig();
		assertEquals(config, testConnector);
		
	}

}
