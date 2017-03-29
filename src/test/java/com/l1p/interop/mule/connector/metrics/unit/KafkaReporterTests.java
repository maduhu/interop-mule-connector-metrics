package com.l1p.interop.mule.connector.metrics.unit;

import java.io.File;
import java.io.OutputStream;
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
import com.codahale.metrics.Reservoir;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Timer;
import com.l1p.interop.mule.connector.metrics.reporter.KafkaReporter;
import com.l1p.interop.mule.connector.metrics.reporter.MetricKafkaProducer;

public class KafkaReporterTests {

	@Test
	public void testConstructor() {
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
		
		String metricsServer = "ec2-35-164-199-6.us-west-2.compute.amazonaws.com:9092";
		String clientId = "test1";
		
		String kafkaTopic = "topic1";
		MetricKafkaProducer metricKafkaProducer = new MetricKafkaProducer(metricsServer, clientId);
		
		KafkaReporter.forRegistry(registry, kafkaTopic, metricKafkaProducer, env, app);
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
		
		String directory = "/tmp";
		String env = "dfsp1-qa";
		String app = "main.app";
		
		String metricsServer = "ec2-35-164-199-6.us-west-2.compute.amazonaws.com:9092";
		String clientId = "test1";
		
		MetricKafkaProducer metricKafkaProducer = new MetricKafkaProducer(metricsServer, clientId);
		
		File file = new File(directory);
		KafkaReporter.Builder kafkaBuilder = KafkaReporter.forRegistry(registry, "p95", metricKafkaProducer, env, app);

		kafkaBuilder.build().report(createGauges(), createCounters(), createdHistogramData(), createMeterData(), createTimerData());
		kafkaBuilder.build().start(100, TimeUnit.MILLISECONDS);
		kafkaBuilder.build().stop();
		
		kafkaBuilder.filter(new MetricFilter() {
			
			@Override
			public boolean matches(String arg0, Metric arg1) {
				return true;
			}
		});
		
		kafkaBuilder.formatFor(Locale.US);
		
		kafkaBuilder.convertRatesTo(TimeUnit.HOURS);
		
		kafkaBuilder.convertDurationsTo(TimeUnit.MINUTES);
		
		
		Clock clock = new Clock() {
			@Override
			public long getTick() {
				return 23;
			}
		};
		
		kafkaBuilder.withClock(clock);
	}
	
	
//	@Test
//	public void testSantize() {
//		KafkaReporter r = new KafkaReporter();
//		
//		kafkaBuilder.filter(new MetricFilter() {
//			
//			@Override
//			public boolean matches(String arg0, Metric arg1) {
//				return false;
//			}
//		});
//	}
	
	
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
		
		SortedMap<String, Meter> meters = new TreeMap<>();
		
		Meter m = new Meter();
		m.mark(123);
		meters.put("p95", m);
		
		m = new Meter();
		m.mark(136);
		meters.put("p95", m);
		
		return meters;
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
	
	
	private <T> SortedMap<String, Counter> createCounters() {
		Counter counter = new Counter();
		counter.inc(10L);
		
		SortedMap<String, Counter> counters = new TreeMap<>();
		counters.put("a1", counter);
		counters.put("b1", counter);
		
		return counters;
		
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


}
