package com.l1p.interop.mule.connector.metrics.unit;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.CsvReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Slf4jReporter;
import com.l1p.interop.mule.connector.metrics.reporter.CsvReporterWithDeltas;
import com.l1p.interop.mule.connector.metrics.reporter.KafkaReporter;
import com.l1p.interop.mule.connector.metrics.reporter.MetricKafkaProducer;
import com.l1p.interop.mule.connector.metrics.reporter.Slf4jReporterWithDeltas;
import com.l1p.interop.mule.connector.metrics.spring.ReporterFactory;

public class ReporterFactoryTests {

	ReporterFactory f;
	
	@Test
	public void testConstructor() {
		f = new ReporterFactory();
		assertTrue(f != null);
	}
	
	
	@Test
	public void testCreateMetricRegistry() {
		MetricRegistry registry = ReporterFactory.createMetricRegistry();
		assertTrue(registry != null);
	}
	
	@Test
	public void testCreateConsoleReporter() {
		ConsoleReporter reporter = ReporterFactory.createConsoleReporter(new MetricRegistry());
	}
	
	
	@Test
	public void testCreateSlf4jReporter() {
		Slf4jReporter reporter = ReporterFactory.createSlf4jReporter(new MetricRegistry());
	}
	
	@Test
	public void testCreateKafkaReporter() {
		String metricsReporterEnvironment="dfsp1-qa";
		String metricsServer = "ec2-35-164-199-6.us-west-2.compute.amazonaws.com:9092";
		String clientId = "test1";
		
		KafkaReporter reporter = ReporterFactory.createKafkaReporter(new MetricRegistry(), "Topic", new MetricKafkaProducer(metricsServer, clientId), metricsReporterEnvironment, "TestApp" );
		
		
	}
	
	
	@Test
	public void testCreateCsvReporter() {
		CsvReporter reporter = ReporterFactory.createCsvReporter( new MetricRegistry(), "/tmp");
		assertTrue(reporter != null);
	}
	
	
	@Test
	public void testCreateCsvReporterWithDeltas() {
		CsvReporterWithDeltas reporter = ReporterFactory.createCsvReporterWithDeltas( new MetricRegistry(), "/tmp", "dfsp1-qa", "main.app");
		assertTrue(reporter != null);
	}
	
	
	@Test
	public void testCreateSlf4ReporterWithDeltas() {
		Slf4jReporterWithDeltas reporter = ReporterFactory.createSlf4jReporterWithDeltas( new MetricRegistry());
		assertTrue(reporter != null);
	}

}
