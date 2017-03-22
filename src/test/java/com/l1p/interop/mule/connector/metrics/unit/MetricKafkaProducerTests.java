package com.l1p.interop.mule.connector.metrics.unit;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.l1p.interop.mule.connector.metrics.reporter.MetricKafkaProducer;

public class MetricKafkaProducerTests {

	@Test
	public void testNoArgConstructor() {
		MetricKafkaProducerTests mkp = new MetricKafkaProducerTests();
		assertTrue(mkp != null);
	}
	
	
	@Test
	public void testArgConstructor() {
		String metricsReporterEnvironment="dfsp1-qa";
		String metricsServer = "ec2-35-164-199-6.us-west-2.compute.amazonaws.com:9092";
		String clientId = "test1";
		
		MetricKafkaProducer mkp = new MetricKafkaProducer(metricsServer, clientId);
		
		assertTrue(mkp != null);
		
		String[] args = {"A", "B", "C"};
		
		mkp.main(args);
		
	}

}
