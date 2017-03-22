package com.l1p.interop.mule.connector.metrics.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.Timer;
import com.l1p.interop.mule.connector.metrics.config.ConnectorConfig;

public class ConnectorConfigTests {

	ConnectorConfig connector;
	
	@Test
	public void testConstructor() {
		connector = new ConnectorConfig();
		assertTrue(connector != null);
	}
	
	
	@Test
	public void testMetricRegistrySetters() {
		connector = new ConnectorConfig();
		connector.setMetricRegistry(new MetricRegistry());
		
		MetricRegistry registry = connector.getMetricRegistry();
		
		assertTrue(connector != null);
		assertTrue(registry != null);
	}
	
	
	@Test
	public void testReporterIntervalSetters() {
		int intervalForTest = 316;
		
		connector = new ConnectorConfig();
		connector.setReporterInterval(intervalForTest);
		
		int intFromGet = connector.getReporterInterval();
		
		assertEquals("test interval", intervalForTest, intFromGet);
	}
	
	
	@Test
	public void testScheduleReporterSetters() {
		connector = new ConnectorConfig();
		List<ScheduledReporter> scheduledReporters = new ArrayList<ScheduledReporter>();
		ScheduledReporter reporter = null;
		
		reporter = createScheduledReporterTestObject();
		scheduledReporters.add(reporter);
		
		connector.setScheduledReporters(scheduledReporters);
		List<ScheduledReporter> scheduledReportersTest = connector.getScheduledReporters();
		
		assertTrue(scheduledReportersTest != null);
		assertEquals(scheduledReportersTest.size(), 1);
	}
	
	
	private ScheduledReporter createScheduledReporterTestObject() {
		
		TimeUnit rateUnit = TimeUnit.DAYS;
		TimeUnit durationUnit = TimeUnit.HOURS;
		
		MetricFilter filter = new MetricFilter() {
			
			@Override
			public boolean matches(String arg0, Metric arg1) {
				System.out.println("equals for Metrics Filter method just called");
				return false;
			}
		};
		
		ScheduledReporter reporter = new ScheduledReporter(new MetricRegistry(), "location1", filter, rateUnit, durationUnit) {
			
			@Override
			public void report(SortedMap<String, Gauge> arg0, SortedMap<String, Counter> arg1,
					SortedMap<String, Histogram> arg2, SortedMap<String, Meter> arg3, SortedMap<String, Timer> arg4) {
				System.out.println("report generated");
				
			}
		};
		
		return reporter;
	}

}
