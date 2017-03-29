package com.l1p.interop.mule.connector.metrics.unit;

import static org.junit.Assert.*;

import java.io.OutputStream;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Reservoir;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Timer;
import com.l1p.interop.mule.connector.metrics.reporter.KVPMetricsReporter;
import com.l1p.interop.mule.connector.metrics.reporter.KVPMetricsReporter.Builder;
import com.l1p.interop.mule.connector.metrics.reporter.KVPMetricsReporter.LoggingLevel;

public class KVPMetricsReporterTests {
	

	@Test
	public void testForRegistry() {
		MetricRegistry registry = new MetricRegistry();
		Timer timer = new Timer();
		timer.update(100L, TimeUnit.MILLISECONDS);
		
		Builder builder = null;
		KVPMetricsReporter reporter;
		
		Counter counter = new Counter();
		counter.inc(1);
		
		registry.register("metrics-csv-reporting-timer", timer);
		registry.register("metrics-csv-reporting-counter", counter);
		
		KVPMetricsReporter.Builder b = KVPMetricsReporter.forRegistry(registry);
		b.markWith(createTestMarker("LogLocation1"));
		
		
		/*
		 * Tests
		 * 
		 */
		builder = b.convertDurationsTo(TimeUnit.HOURS);
		assertTrue("ensure conversion durationsTo was set", builder != null);

		builder = b.convertRatesTo(TimeUnit.SECONDS);
		assertTrue("ensure conversion ratesTo was set", builder != null);
		
		builder = b.filter( createTestMetricFilter() );
		assertTrue("ensure Metrics Filter was set", builder != null);
		
		builder = b.outputTo(createTestLogger());
		assertTrue("ensure outputTo was set", builder != null);
		
		assertTrue("ensure marker was set", builder != null);
		
		builder = b.withLoggingLevel(LoggingLevel.DEBUG);
		reporter = b.build();
		assertTrue("ensure Logging Level for DEBUG was set", builder != null);
		
		builder = b.withLoggingLevel(LoggingLevel.ERROR);
		reporter = b.build();
		assertTrue("ensure Logging Level for ERROR was set", builder != null);

		builder = b.withLoggingLevel(LoggingLevel.INFO);
		reporter = b.build();
		assertTrue("ensure Logging Level for INFO was set", builder != null);
		
		builder = b.withLoggingLevel(LoggingLevel.TRACE);
		reporter = b.build();
		assertTrue("ensure Logging Level for TRACE was set", builder != null);
		
		builder = b.withLoggingLevel(LoggingLevel.WARN);
		assertTrue("ensure Logging Level for WARN  was set", builder != null);
		
		// Need to get a marker set for the report test below.
		b.markWith( createTestMarker("Marker1"));
		reporter = b.build();
		assertTrue("ensure build worked", builder != null);

		reporter.report(createGauges(), createCounters(), createdHistogramData(), createMeterData(), createTimerData());
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		reporter.report(createGauges(), createCounters(), createdHistogramData(), createMeterData(), createTimerData());
        
	}
	
	
	@Test
	public void testReport() {
		
		MetricRegistry registry = new MetricRegistry();
		Timer timer = new Timer();
		timer.update(100L, TimeUnit.MILLISECONDS);
		
		Counter counter = new Counter();
		counter.inc(1);
		
		registry.register("metrics-csv-reporting-timer", timer);
		registry.register("metrics-csv-reporting-counter", counter);
		
//		KVPMetricsReporter reporter = new KVPMetricsReporter(registry, KVPMetricsReporter., createTestMarker(), TimeUnit.MILLISECONDS, TimeUnit.HOURS, createTestMetricFilter());
	}
	
	
	private Marker createTestMarker(String name) {
		
		Marker marker = MarkerFactory.getMarker(name);
		return marker;
	}
	
	
	private SortedMap<String, Timer> createTimerData() {
		SortedMap<String, Timer> timers = new TreeMap<>();
		
		Timer timer = new Timer();
		timer.update(105L, TimeUnit.MILLISECONDS);
		timers.put("p95", timer);

		timer = new Timer();
		timer.update(119L, TimeUnit.MILLISECONDS);
		timers.put("p95", timer);
		
		System.out.println("Just created timers. Count = " + timers.size());
		
		return timers;
	}
	
	
	private SortedMap<String, Meter> createMeterData() {
		
		SortedMap<String, Meter> meters = new TreeMap<>();
		
		Meter m = new Meter();
		m.mark(123);
		meters.put("p95", m);
		
		m = new Meter();
		m.mark(136);
		meters.put("p95", m);
		
		return meters;
	}
	
	
	private SortedMap<String, Histogram> createdHistogramData() {
		
		Reservoir reservoir = new Reservoir() {
			
			@Override
			public void update(long arg0) {
				// TODO Auto-generated method stub
				System.out.println("update called, value = " + arg0);
			}
			
			@Override
			public int size() {
				// TODO Auto-generated method stub
				return 2;
			}
			
			@Override
			public Snapshot getSnapshot() {
				// TODO Auto-generated method stub
				Snapshot s = new Snapshot() {
					
					@Override
					public int size() {
						// TODO Auto-generated method stub
						return 2;
					}
					
					@Override
					public long[] getValues() {
						// TODO Auto-generated method stub
						long[] longs = new long[2];
						longs[0] = 2;
						longs[1] = 4;
						return longs;
					}
					
					@Override
					public double getValue(double arg0) {
						double d = 3;
						return arg0;
					}
					
					@Override
					public double getStdDev() {
						double d = 1.12232;
						return d;
					}
					
					@Override
					public long getMin() {
						long l = 2;
						return l;
					}
					
					@Override
					public double getMean() {
						double d = 3;
						return d;
					}
					
					@Override
					public long getMax() {
						long l = 4;
						return l;
					}
					
					@Override
					public void dump(OutputStream arg0) {
						// TODO Auto-generated method stub
						System.out.println("dump called");
					}
				};
				return s;
			}
		};
		
		Histogram h = new Histogram(reservoir);
		SortedMap<String, Histogram> histograms = new TreeMap<>();
		histograms.put("p95", h);
		
		return histograms;
	}


	private <T> SortedMap<String, Counter> createCounters() {
		Counter counter = new Counter();
		counter.inc(10L);

		
		SortedMap<String, Counter> counters = new TreeMap<>();
		counters.put("a1", counter);
		
		counter = new Counter();
		counter.inc(13);
		counters.put("b1", counter);
		
		return counters;
		
	}
	
	
	private <T> SortedMap<String, Gauge> createGauges() {
		SortedMap<String, Gauge> gauges = new TreeMap<>();
		
		Gauge g = new Gauge<T>() {

			@Override
			public T getValue() {
				return (T) "this is a gague";
			}
			
		};
		
		gauges.put("996", g);
		
		return gauges;
	}
	
	private Logger createTestLogger() {
		Logger logger = LoggerFactory.getLogger("metrics");
		return logger;
	}
	
	private MetricFilter createTestMetricFilter() {
		
		MetricFilter filter = new MetricFilter() {
			
			@Override
			public boolean matches(String arg0, Metric arg1) {
				return true;
			}
		};
		
		return filter;
	}
	
	
	

}
