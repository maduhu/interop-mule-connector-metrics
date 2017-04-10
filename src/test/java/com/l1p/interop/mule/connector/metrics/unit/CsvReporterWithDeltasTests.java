package com.l1p.interop.mule.connector.metrics.unit;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Locale;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.codahale.metrics.Clock;
import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.l1p.interop.mule.connector.metrics.reporter.CsvReporterWithDeltas;
import com.l1p.interop.mule.connector.metrics.reporter.CsvReporterWithDeltas.Builder;

public class CsvReporterWithDeltasTests {

	@Test
	public void test() {
		
		MetricRegistry registry = new MetricRegistry();
		String directory = "/tmp";
		String env = "dfsp1-qa";
		String app = "main.app";
		
		File file = new File(directory);
		Builder reporter = CsvReporterWithDeltas.forRegistry(registry, file, env, app);
		assertTrue(reporter != null);
	}
	
	
	@Test
	public void testFormatForReport() {
		MetricRegistry registry = new MetricRegistry();
		Timer timer = new Timer();
		timer.update(100L, TimeUnit.MILLISECONDS);
		
		Counter counter = new Counter();
		counter.inc(1);
		
		registry.register("metrics-csv-reporting-timer", timer);
		registry.register("metrics-csv-reporting-counter", counter);
		
		String directory = "/tmp";
		String env = "dfsp1-qa";
		String app = "main.app";
		
		TimeUnit rateUnitDays = TimeUnit.DAYS;
		TimeUnit rateUnitHours = TimeUnit.HOURS;
		TimeUnit durationUnit = TimeUnit.HOURS;
		
		Clock clock = new Clock() {
			
			@Override
			public long getTick() {
				return 0;
			}
		};
		
		System.out.println("clock getTime() = " + clock.getTime());
		System.out.println("clock in milli: " + TimeUnit.MILLISECONDS.toSeconds(clock.getTime() ));
		assertTrue("ensure clock has time", clock.getTime() > 0 );
		
		File file = new File(directory);
		Builder reporter = CsvReporterWithDeltas.forRegistry(registry, file, env, app);
		
		assertTrue(reporter != null);
		
		reporter = reporter.formatFor(Locale.US);
		assertTrue(reporter != null);
		
		reporter = reporter.convertRatesTo(rateUnitDays);
		assertTrue(reporter != null);
		
		reporter = reporter.convertDurationsTo(rateUnitHours);
		assertTrue(reporter != null);
		
		reporter = reporter.withClock(clock);
		assertTrue(reporter != null);
		
		reporter = reporter.filter(new MetricFilter() {
			
			@Override
			public boolean matches(String arg0, Metric arg1) {
				return false;
			}
		});
		assertTrue(reporter != null);
		
		File newDirectory = new File("/tmp");
		
		reporter.build(newDirectory).report(createGauges(), createCounters(), createdHistogramData(), createMeterData(), createTimerData());
		
		// Seems like we need to add some asserts here to ensure that we got valid output.  
		
	}
	
	
	private SortedMap<String, Timer> createTimerData() {
		SortedMap<String, Timer> timers = new TreeMap<>();
		Timer timer = new Timer();
		timer.update(105L, TimeUnit.MILLISECONDS);
		timers.put("p95", timer);

		timer = new Timer();
		timer.update(109L, TimeUnit.MILLISECONDS);
		timers.put("p96", timer);
		
		return timers;
	}
	
	private SortedMap<String, Meter> createMeterData() {
		return null;
	}
	
	private <T> SortedMap<String, Gauge> createGauges() {
		SortedMap<String, Gauge> gauges = new TreeMap<>();
		
		Gauge g = new Gauge<T>() {

			@Override
			public T getValue() {
				// TODO Auto-generated method stub
				return null;
			}
			
		};
		
		return gauges;
	}
	
	
	private <T> SortedMap<String, Counter> createCounters() {
		Counter counter = new Counter();
		counter.inc(10L);
		
		SortedMap<String, Counter> counters = new TreeMap<>();
		counters.put("a1", counter);
		counters.put("b1", counter);
		
		return counters;
		
	}
	
	private SortedMap<String, Histogram> createdHistogramData() {
		
		return null;
	}

	

}
