package com.l1p.interop.mule.connector.metrics.reporter;

import com.codahale.metrics.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.Formatter;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

import static com.l1p.interop.mule.connector.metrics.reporter.MetricType.*;

/**
 * A reporter which creates a comma-separated values kafka messages of the measurements for each metric.
 */
public class KafkaReporter extends ScheduledReporter {

  private static final Logger LOGGER = LoggerFactory.getLogger(KafkaReporter.class);
  private static final Charset UTF_8 = Charset.forName("UTF-8");
  private final String kafkaTopic;
  private final Locale locale;
  private final Clock clock;
  private final MetricKafkaProducer metricKafkaProducer;
  private final String env;
  private final String app;

  private KafkaReporter(MetricRegistry registry,
                        Locale locale,
                        TimeUnit rateUnit,
                        TimeUnit durationUnit,
                        Clock clock,
                        MetricFilter filter,
                        String kafkaTopic,
                        MetricKafkaProducer metricKafkaProducer,
                        String env,
                        String app) {
    super(registry, "kafka-reporter", filter, rateUnit, durationUnit);
    this.locale = locale;
    this.clock = clock;
    this.kafkaTopic = kafkaTopic;
    this.metricKafkaProducer = metricKafkaProducer;
    this.env = env;
    this.app = app;
  }

  /**
   * Returns a new {@link KafkaReporter.Builder} for {@link KafkaReporter}.
   *
   * @param registry the registry to report
   * @return a {@link KafkaReporter.Builder} instance for a {@link KafkaReporter}
   */
  public static KafkaReporter.Builder forRegistry(MetricRegistry registry, String kafkaTopic, MetricKafkaProducer metricKafkaProducer, String env, String app) {
    return new KafkaReporter.Builder(registry, kafkaTopic, metricKafkaProducer, env, app);
  }

  @Override
  public void report(SortedMap<String, Gauge> gauges,
                     SortedMap<String, Counter> counters,
                     SortedMap<String, Histogram> histograms,
                     SortedMap<String, Meter> meters,
                     SortedMap<String, Timer> timers) {
    final long timestamp = TimeUnit.MILLISECONDS.toSeconds(clock.getTime());

    for (Map.Entry<String, Gauge> entry : gauges.entrySet()) {
      reportGauge(timestamp, entry.getKey(), entry.getValue());
    }

    for (Map.Entry<String, Counter> entry : counters.entrySet()) {
      reportCounter(timestamp, entry.getKey(), entry.getValue());
    }

    for (Map.Entry<String, Histogram> entry : histograms.entrySet()) {
      reportHistogram(timestamp, entry.getKey(), entry.getValue());
    }

    for (Map.Entry<String, Meter> entry : meters.entrySet()) {
      reportMeter(timestamp, entry.getKey(), entry.getValue());
    }

    for (Map.Entry<String, Timer> entry : timers.entrySet()) {
      reportTimer(timestamp, entry.getKey(), entry.getValue());
    }
  }

  private void reportTimer(long timestamp, String name, Timer timer) {
    final Snapshot snapshot = timer.getSnapshot();

    report(timestamp,
        name,
        TIMER,
        "%d,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,calls/%s,%s",
        timer.getCount(),
        convertDuration(snapshot.getMax()),
        convertDuration(snapshot.getMean()),
        convertDuration(snapshot.getMin()),
        convertDuration(snapshot.getStdDev()),
        convertDuration(snapshot.getMedian()),
        convertDuration(snapshot.get75thPercentile()),
        convertDuration(snapshot.get95thPercentile()),
        convertDuration(snapshot.get98thPercentile()),
        convertDuration(snapshot.get99thPercentile()),
        convertDuration(snapshot.get999thPercentile()),
        convertRate(timer.getMeanRate()),
        convertRate(timer.getOneMinuteRate()),
        convertRate(timer.getFiveMinuteRate()),
        convertRate(timer.getFifteenMinuteRate()),
        getRateUnit(),
        getDurationUnit());
  }

  private void reportMeter(long timestamp, String name, Meter meter) {
    report(timestamp,
        name,
        METER,
        "%d,%f,%f,%f,%f,events/%s",
        meter.getCount(),
        convertRate(meter.getMeanRate()),
        convertRate(meter.getOneMinuteRate()),
        convertRate(meter.getFiveMinuteRate()),
        convertRate(meter.getFifteenMinuteRate()),
        getRateUnit());
  }

  private void reportHistogram(long timestamp, String name, Histogram histogram) {
    final Snapshot snapshot = histogram.getSnapshot();

    report(timestamp,
        name,
        HISTOGRAM,
        "%d,%d,%f,%d,%f,%f,%f,%f,%f,%f,%f",
        histogram.getCount(),
        snapshot.getMax(),
        snapshot.getMean(),
        snapshot.getMin(),
        snapshot.getStdDev(),
        snapshot.getMedian(),
        snapshot.get75thPercentile(),
        snapshot.get95thPercentile(),
        snapshot.get98thPercentile(),
        snapshot.get99thPercentile(),
        snapshot.get999thPercentile());
  }

  private void reportCounter(long timestamp, String name, Counter counter) {
    report(timestamp, name, COUNTER, "%d", counter.getCount());
  }

  private void reportGauge(long timestamp, String name, Gauge gauge) {
    report(timestamp, name, GAUGE, "%s", gauge.getValue());
  }

  private void report(long timestamp, String name, MetricType metricType, String line, Object... values) {
    final Formatter formatter = new Formatter(locale);
    final String metric = formatter.format(String.format(locale, "%s,%s,%s,%s,%d,%s", metricType, env, app, name, timestamp, line), values).toString();
    metricKafkaProducer.send(kafkaTopic, name, metric);

  }

  protected String sanitize(String name) {
    return name;
  }

  @Override
  public void stop() {
    try {
      super.stop();
    } finally {
      metricKafkaProducer.close();
    }
  }

  /**
   * A builder for {@link KafkaReporter} instances. Defaults to using the default locale, converting
   * rates to events/second, converting durations to milliseconds, and not filtering metrics.
   */
  public static class Builder {
    private final MetricRegistry registry;
    private final String env;
    private final String app;
    private Locale locale;
    private TimeUnit rateUnit;
    private TimeUnit durationUnit;
    private Clock clock;
    private MetricFilter filter;
    private String kafkaTopic;
    private MetricKafkaProducer metricKafkaProducer;

    private Builder(MetricRegistry registry, String kafkaTopic, MetricKafkaProducer metricKafkaProducer, String env, String app) {
      this.registry = registry;
      this.env = env;
      this.app = app;
      this.locale = Locale.getDefault();
      this.rateUnit = TimeUnit.SECONDS;
      this.durationUnit = TimeUnit.MILLISECONDS;
      this.clock = Clock.defaultClock();
      this.filter = MetricFilter.ALL;
      this.kafkaTopic = kafkaTopic;
      this.metricKafkaProducer = metricKafkaProducer;
    }

    /**
     * Format numbers for the given {@link Locale}.
     *
     * @param locale a {@link Locale}
     * @return {@code this}
     */
    public KafkaReporter.Builder formatFor(Locale locale) {
      this.locale = locale;
      return this;
    }

    /**
     * Convert rates to the given time unit.
     *
     * @param rateUnit a unit of time
     * @return {@code this}
     */
    public KafkaReporter.Builder convertRatesTo(TimeUnit rateUnit) {
      this.rateUnit = rateUnit;
      return this;
    }

    /**
     * Convert durations to the given time unit.
     *
     * @param durationUnit a unit of time
     * @return {@code this}
     */
    public KafkaReporter.Builder convertDurationsTo(TimeUnit durationUnit) {
      this.durationUnit = durationUnit;
      return this;
    }

    /**
     * Use the given {@link Clock} instance for the time.
     *
     * @param clock a {@link Clock} instance
     * @return {@code this}
     */
    public KafkaReporter.Builder withClock(Clock clock) {
      this.clock = clock;
      return this;
    }

    /**
     * Only report metrics which match the given filter.
     *
     * @param filter a {@link MetricFilter}
     * @return {@code this}
     */
    public KafkaReporter.Builder filter(MetricFilter filter) {
      this.filter = filter;
      return this;
    }

    /**
     * Builds a {@link KafkaReporter} with the given properties, sending messages to the given topic.
     *
     * @return a {@link KafkaReporter}
     */
    public KafkaReporter build() {
      return new KafkaReporter(registry,
          locale,
          rateUnit,
          durationUnit,
          clock,
          filter,
          kafkaTopic,
          metricKafkaProducer,
          env,
          app);
    }
  }

}
