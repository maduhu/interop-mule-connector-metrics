package com.l1p.interop.mule.connector.metrics.spring;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Slf4jReporter;
import com.l1p.interop.mule.connector.metrics.reporter.KafkaReporter;
import com.l1p.interop.mule.connector.metrics.reporter.MetricKafkaProducer;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class ReporterFactory {

  public static MetricRegistry createMetricRegistry() {
    final MetricRegistry registry = new MetricRegistry();
//    registry.register(MetricRegistry.name("jvm", "gc"), new GarbageCollectorMetricSet());
//    registry.register(MetricRegistry.name("jvm", "memory"), new MemoryUsageGaugeSet());
//    registry.register(MetricRegistry.name("jvm", "classloading"), new ClassLoadingGaugeSet());
//    registry.register(MetricRegistry.name("jvm", "thread-states"), new ThreadStatesGaugeSet());
//    registry.register(MetricRegistry.name("jvm", "file", "usage"), new FileDescriptorRatioGauge());

    return registry;
  }

  public static final ConsoleReporter createConsoleReporter(MetricRegistry metricRegistry) {
    return ConsoleReporter.forRegistry(metricRegistry)
        .convertRatesTo(TimeUnit.SECONDS)
        .convertDurationsTo(TimeUnit.MILLISECONDS)
        .build();
  }

  public static Slf4jReporter createSlf4jReporter(MetricRegistry metricRegistry) {
    return Slf4jReporter.forRegistry(metricRegistry)
        .outputTo(LoggerFactory.getLogger("com.example.metrics"))
        .convertRatesTo(TimeUnit.SECONDS)
        .convertDurationsTo(TimeUnit.MILLISECONDS)
        .build();
  }

  public static final KafkaReporter createKafkaReporter(MetricRegistry metricRegistry, String globalPrefix, MetricKafkaProducer producer, String env, String app) {
    return KafkaReporter.forRegistry(metricRegistry, globalPrefix, producer, env, app)
        .convertRatesTo(TimeUnit.SECONDS)
        .convertDurationsTo(TimeUnit.MILLISECONDS)
        .build();
  }

}
