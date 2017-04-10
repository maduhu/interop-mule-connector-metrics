package com.l1p.interop.mule.connector.metrics.unit;

import static org.junit.Assert.*;

import org.junit.Test;

import com.l1p.interop.mule.connector.metrics.reporter.MetricType;

public class MetricTypeEnumTests {

	@Test
	public void testCounter() {
		MetricType type = MetricType.COUNTER;
		assertEquals(type, MetricType.COUNTER);
	}
	
	
	@Test
	public void testTimer() {
		MetricType type = MetricType.TIMER;
		assertEquals(type, MetricType.TIMER);
	}
	
	
	@Test
	public void testHistogram() {
		MetricType type = MetricType.HISTOGRAM;
		assertEquals(type, MetricType.HISTOGRAM);
	}
	
	
	@Test
	public void testMeter() {
		MetricType type = MetricType.METER;
		assertEquals(type, MetricType.METER);
	}
	
	
	@Test
	public void testGauge() {
		MetricType type = MetricType.GAUGE;
		assertEquals(type, MetricType.GAUGE);
	}

}
