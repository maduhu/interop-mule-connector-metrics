package com.l1p.interop.mule.connector.metrics.automation.functional;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.l1p.interop.mule.connector.metrics.reporter.KafkaReporter;
import com.l1p.interop.mule.connector.metrics.reporter.MetricKafkaProducer;
import com.l1p.interop.mule.connector.metrics.spring.ReporterFactory;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Random;

import static java.util.concurrent.TimeUnit.SECONDS;

@Ignore
public class KafkaReporterFunctionalTest {

  private String kafkaTopic = "test";
  private String kafkaServer = "ec2-52-26-168-223.us-west-2.compute.amazonaws.com:9092";

  @Test
  public void generateSomeMetrics() throws Exception {
    final MetricRegistry metricRegistry = new MetricRegistry();
    final KafkaReporter kafkaReporter = ReporterFactory.createKafkaReporter(metricRegistry, kafkaTopic, new MetricKafkaProducer(kafkaServer, "reporter-functional-test"), "ft-env", "ft-app");
    kafkaReporter.start(3, SECONDS);
    final ConsoleReporter consoleReporter = ReporterFactory.createConsoleReporter(metricRegistry);
    consoleReporter.start(3, SECONDS);

    final Counter counter = metricRegistry.counter("my.counter");
    final Timer timer = metricRegistry.timer("my.timer");
    final Random random = new Random(System.currentTimeMillis());

    for (int i = 0; i < 10; i++) {
      counter.inc(random.nextInt(10));
      final Timer.Context time = timer.time();
      Thread.sleep(random.nextInt(250));
      time.stop();
      Thread.sleep(SECONDS.toMillis(7));
    }

    kafkaReporter.close();
    consoleReporter.close();
  }

}
