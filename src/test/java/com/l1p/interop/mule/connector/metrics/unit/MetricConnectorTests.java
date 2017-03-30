package com.l1p.interop.mule.connector.metrics.unit;

import static com.codahale.metrics.MetricRegistry.name;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mule.api.MuleContext;
import org.mule.api.MuleEvent;
import org.mule.component.SimpleCallableJavaComponentTestCase;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Timer;
import com.codahale.metrics.Timer.Context;
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
	
	
	@Test
	public void testSnapTime() {
		MetricsConnector c = new MetricsConnector();
		String data = "this is a test mule event";
		MuleEvent event = null;
		try {
			event = super.getTestEvent(data);
		} catch (Exception e) {
			e.printStackTrace();
			fail("failed to run test:  error = " + e.getMessage());
		}

		c.snapTime(event, "snapTimeKey");
	}
	
	
	@Test
	public void testTime() {
		MetricsConnector c = new MetricsConnector();
		ConnectorConfig config = new ConnectorConfig();
		
		MetricRegistry registry = new MetricRegistry();
		c.setConfig(config);
		
		config.setMetricRegistry(registry);
		
		Timer timer = new Timer();
		timer.update(100L, TimeUnit.NANOSECONDS);

		String category = "testCategoryKey";
		String metricKey = "testMetricKey";
		
		String beginSnapKey = "beginSnapKey";
		String endSnapKey = "endSnapKey";

		final Timer timer2 = config.getMetricRegistry().timer( name(category, metricKey));
		assertTrue("timer was creagted", timer2 != null);
		
		String data = "this is a test mule event";
		MuleEvent event = null;
		try {
			event = super.getTestEvent(data);
		} catch (Exception e) {
			fail("failed to run test:  error = " + e.getMessage());
		}
		
		c.setConfig(config);

		event.setSessionVariable(beginSnapKey, 90L);
		event.setSessionVariable(endSnapKey, 100L);
		
		c.time(event, category, metricKey, beginSnapKey, endSnapKey);
		
		Timer testTimer = registry.timer( name(category, metricKey) );
		
		Snapshot ss = testTimer.getSnapshot();
		long[] values = ss.getValues();

		assertEquals("timer count", testTimer.getCount(), 1);
		assertEquals("ensure that the timer and mule variable is set", values[0], 10);
		
	}
	
	
	@Test
	public void testIncCount() {
		MetricsConnector c = new MetricsConnector();
		ConnectorConfig config = new ConnectorConfig();
		Counter counter = null;
		
		MetricRegistry registry = new MetricRegistry();
		c.setConfig(config);
		
		config.setMetricRegistry(registry);
		
		String category = "testCategoryKey";
		String metricKey = "testMetricKey";
		
		List<String> metricKeys = new ArrayList<String>();

		String key1 = name(category, "1");
		String key2 = name(category, "2");
		
		metricKeys.add(key1);
		metricKeys.add(key2);
		
		// Create counter 1
		counter = new Counter();
		counter.inc(10L);
		
		registry.register(key1, counter);
		
		// Create counter 1
		counter = new Counter();
		counter.inc(15L);
		registry.register(key2, counter);

		// Execute the actual test code.
		c.incCount(category, metricKeys, 25);
		c.decCount(category, metricKeys, 19);
		
		
	}
	

}
