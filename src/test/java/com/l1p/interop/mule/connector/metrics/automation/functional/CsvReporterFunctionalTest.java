package com.l1p.interop.mule.connector.metrics.automation.functional;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.l1p.interop.mule.connector.metrics.reporter.CsvReporterWithDeltas;
import com.l1p.interop.mule.connector.metrics.spring.ReporterFactory;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Random;

import static java.util.concurrent.TimeUnit.SECONDS;

@Ignore
public class CsvReporterFunctionalTest {

  private String directory = "MetricsTest";

  @Test
  public void testCsvMetrics() throws Exception {

    final MetricRegistry metricRegistry = new MetricRegistry();
    final ConsoleReporter consoleReporter = ReporterFactory.createConsoleReporter(metricRegistry);

    final CsvReporterWithDeltas csvMetricReporter = ReporterFactory.createCsvReporterWithDeltas(metricRegistry, directory, "ft-env", "ft-app");

    csvMetricReporter.start(3, SECONDS);
    consoleReporter.start(3, SECONDS);

    final Counter counter = metricRegistry.counter("metrics-counter-with-deltas");
    final Timer timer = metricRegistry.timer("metrics-timer-with-deltas");
    final Random random = new Random(System.currentTimeMillis());

    for (int i = 0; i < 5; i++) {
      counter.inc(random.nextInt(10));
      final Timer.Context time = timer.time();
      Thread.sleep(random.nextInt(50));
      time.stop();
      Thread.sleep(SECONDS.toMillis(7));
    }

    csvMetricReporter.close();
    consoleReporter.close();
  }

}
