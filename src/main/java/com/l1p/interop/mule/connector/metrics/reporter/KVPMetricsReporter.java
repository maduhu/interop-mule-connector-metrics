package com.l1p.interop.mule.connector.metrics.reporter;

import com.codahale.metrics.*;
import com.codahale.metrics.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

/**
 * A reporter class for logging metrics values to a SLF4J {@link org.slf4j.Logger} periodically, similar to
 * {@link com.codahale.metrics.ConsoleReporter} or {@link com.codahale.metrics.CsvReporter}, but using the SLF4J framework instead. It also
 * supports specifying a {@link org.slf4j.Marker} instance that can be used by custom appenders and filters
 * for the bound logging toolkit to further process metrics reports.
 */
public class KVPMetricsReporter extends ScheduledReporter {

    private final Map<String, Long> timerCounts = new HashMap<String, Long>();
    long lastTimestamp = 0;
    private final Clock clock;

    /**
     * Returns a new {@link Builder} for {@link com.codahale.metrics.Slf4jReporter}.
     *
     * @param registry the registry to report
     * @return a {@link Builder} instance for a {@link com.codahale.metrics.Slf4jReporter}
     */
    public static Builder forRegistry(MetricRegistry registry) {
        return new Builder(registry);
    }

    public enum LoggingLevel {TRACE, DEBUG, INFO, WARN, ERROR}

    /**
     * A builder for {@link com.codahale.metrics.Slf4jReporter} instances. Defaults to logging to {@code metrics}, not
     * using a marker, converting rates to events/second, converting durations to milliseconds, and
     * not filtering metrics.
     */
    public static class Builder {
        private final MetricRegistry registry;
        private Logger logger;
        private LoggingLevel loggingLevel;
        private Marker marker;
        private TimeUnit rateUnit;
        private TimeUnit durationUnit;
        private MetricFilter filter;

        private Builder(MetricRegistry registry) {
            this.registry = registry;
            this.logger = LoggerFactory.getLogger("metrics");
            this.marker = null;
            this.rateUnit = TimeUnit.SECONDS;
            this.durationUnit = TimeUnit.MILLISECONDS;
            this.filter = MetricFilter.ALL;
            this.loggingLevel = LoggingLevel.INFO;
        }

        /**
         * Log metrics to the given logger.
         *
         * @param logger an SLF4J {@link org.slf4j.Logger}
         * @return {@code this}
         */
        public Builder outputTo(Logger logger) {
            this.logger = logger;
            return this;
        }

        /**
         * Mark all logged metrics with the given marker.
         *
         * @param marker an SLF4J {@link org.slf4j.Marker}
         * @return {@code this}
         */
        public Builder markWith(Marker marker) {
            this.marker = marker;
            return this;
        }

        /**
         * Convert rates to the given time unit.
         *
         * @param rateUnit a unit of time
         * @return {@code this}
         */
        public Builder convertRatesTo(TimeUnit rateUnit) {
            this.rateUnit = rateUnit;
            return this;
        }

        /**
         * Convert durations to the given time unit.
         *
         * @param durationUnit a unit of time
         * @return {@code this}
         */
        public Builder convertDurationsTo(TimeUnit durationUnit) {
            this.durationUnit = durationUnit;
            return this;
        }

        /**
         * Only report metrics which match the given filter.
         *
         * @param filter a {@link com.codahale.metrics.MetricFilter}
         * @return {@code this}
         */
        public Builder filter(MetricFilter filter) {
            this.filter = filter;
            return this;
        }

        /**
         * Use Logging Level when reporting.
         *
         * @param loggingLevel a (@link Slf4jReporter.LoggingLevel}
         * @return {@code this}
         */
        public Builder withLoggingLevel(LoggingLevel loggingLevel) {
            this.loggingLevel = loggingLevel;
            return this;
        }

        /**
         * Builds a {@link com.codahale.metrics.Slf4jReporter} with the given properties.
         *
         * @return a {@link com.codahale.metrics.Slf4jReporter}
         */
        public KVPMetricsReporter build() {
            LoggerProxy loggerProxy;
            switch (loggingLevel) {
                case TRACE:
                    loggerProxy = new TraceLoggerProxy(logger);
                    break;
                case INFO:
                    loggerProxy = new InfoLoggerProxy(logger);
                    break;
                case WARN:
                    loggerProxy = new WarnLoggerProxy(logger);
                    break;
                case ERROR:
                    loggerProxy = new ErrorLoggerProxy(logger);
                    break;
                default:
                case DEBUG:
                    loggerProxy = new DebugLoggerProxy(logger);
                    break;
            }
            return new KVPMetricsReporter(registry, loggerProxy, marker, rateUnit, durationUnit, filter);
        }
    }

    private final LoggerProxy loggerProxy;
    private final Marker marker;

    private KVPMetricsReporter(MetricRegistry registry,
                               LoggerProxy loggerProxy,
                               Marker marker,
                               TimeUnit rateUnit,
                               TimeUnit durationUnit,
                               MetricFilter filter) {
        super(registry, "logger-reporter", filter, rateUnit, durationUnit);
        this.loggerProxy = loggerProxy;
        this.marker = marker;
        this.clock = Clock.defaultClock();
    }

    @Override
    public void report(SortedMap<String, Gauge> gauges,
                       SortedMap<String, Counter> counters,
                       SortedMap<String, Histogram> histograms,
                       SortedMap<String, Meter> meters,
                       SortedMap<String, Timer> timers) {

        final long timestamp = TimeUnit.MILLISECONDS.toSeconds(clock.getTime());

//        for (Entry<String, Gauge> entry : gauges.entrySet()) {
//            logGauge(entry.getKey(), entry.getValue());
//        }

        for (Entry<String, Counter> entry : counters.entrySet()) {
            logCounter(entry.getKey(), entry.getValue());
        }

//        for (Entry<String, Histogram> entry : histograms.entrySet()) {
//            logHistogram(entry.getKey(), entry.getValue());
//        }
//
//        for (Entry<String, Meter> entry : meters.entrySet()) {
//            logMeter(entry.getKey(), entry.getValue());
//        }

        if ( valueChanged( timers.entrySet() ) ) {
            for (Entry<String, Timer> entry : timers.entrySet()) {
                logTimer(timestamp, entry.getKey(), entry.getValue());
            }
        }

        this.lastTimestamp = timestamp;

    }

    /**
     * Hack to only report data when values have changed
     *
     * @param timers
     * @return
     */
    private boolean valueChanged( Set<Entry<String, Timer>> timers ) {

        for (Entry<String, Timer> entry : timers) {
            String name = entry.getKey();
            Timer nextTimer = entry.getValue();
            Long lastSample = timerCounts.get( name );
            if ( lastSample == null || ( lastSample.compareTo( nextTimer.getCount() ) != 0 ) ) {
                return true;
            }
        }

        return false;
    }

    /**
     * todo - override Timer to allow min/max/mean to be for current sample
     *
     *
     * @param timestamp
     * @param name
     * @param timer
     */
    private void logTimer(long timestamp, String name, Timer timer) {
        final Snapshot snapshot = timer.getSnapshot();
        Long totalCount = timer.getCount();

        double rate = 0.0d;
        long count = 0;
        long elapsed = 0;
        if ( lastTimestamp > 0 ) {
            elapsed = timestamp - lastTimestamp;
            Long lastSample = timerCounts.get( name );

            if ( elapsed > 0 && lastSample != null ) {
                count = ( totalCount - lastSample );
                rate = count / (double)elapsed;
            }
        }

        timerCounts.put( name, totalCount );
        loggerProxy.log(marker,
                "type=TIMER, category={}, elapsed={}, count={}, totalCount={}, rate={}, m1={}, min={}, max={}, mean={}, " +
                        "p75={}, p95={}, p98={}, p99={}, p999={}, " +
                        "rate_unit={}, duration_unit={}",
                name,
                elapsed,
                count,
                timer.getCount(),
                rate,
                convertRate(timer.getOneMinuteRate()),
                convertDuration(snapshot.getMin()),
                convertDuration(snapshot.getMax()),
                convertDuration(snapshot.getMean()),
                convertDuration(snapshot.get75thPercentile()),
                convertDuration(snapshot.get95thPercentile()),
                convertDuration(snapshot.get98thPercentile()),
                convertDuration(snapshot.get99thPercentile()),
                convertDuration(snapshot.get999thPercentile()),
                getRateUnit(),
                getDurationUnit());
    }

    private void logMeter(String name, Meter meter) {
        loggerProxy.log(marker,
                "type=METER, name={}, count={}, mean_rate={}, m1={}, m5={}, m15={}, rate_unit={}",
                name,
                meter.getCount(),
                convertRate(meter.getMeanRate()),
                convertRate(meter.getOneMinuteRate()),
                convertRate(meter.getFiveMinuteRate()),
                convertRate(meter.getFifteenMinuteRate()),
                getRateUnit());
    }

    private void logHistogram(String name, Histogram histogram) {
        final Snapshot snapshot = histogram.getSnapshot();
        loggerProxy.log(marker,
                "type=HISTOGRAM, name={}, count={}, min={}, max={}, mean={}, stddev={}, " +
                        "median={}, p75={}, p95={}, p98={}, p99={}, p999={}",
                name,
                histogram.getCount(),
                snapshot.getMin(),
                snapshot.getMax(),
                snapshot.getMean(),
                snapshot.getStdDev(),
                snapshot.getMedian(),
                snapshot.get75thPercentile(),
                snapshot.get95thPercentile(),
                snapshot.get98thPercentile(),
                snapshot.get99thPercentile(),
                snapshot.get999thPercentile());
    }

    private void logCounter(String name, Counter counter) {
        loggerProxy.log(marker, "type=COUNTER, name={}, count={}", name, counter.getCount());
    }

    private void logGauge(String name, Gauge gauge) {
        loggerProxy.log(marker, "type=GAUGE, name={}, value={}", name, gauge.getValue());
    }

    @Override
    protected String getRateUnit() {
        return "events/" + super.getRateUnit();
    }

    /* private class to allow logger configuration */
    static abstract class LoggerProxy {
        protected final Logger logger;

        public LoggerProxy(Logger logger) {
            this.logger = logger;
        }

        abstract void log(Marker marker, String format, Object... arguments);
    }

    /* private class to allow logger configuration */
    private static class DebugLoggerProxy extends LoggerProxy {
        public DebugLoggerProxy(Logger logger) {
            super(logger);
        }

        @Override
        public void log(Marker marker, String format, Object... arguments) {
            logger.debug(marker, format, arguments);
        }
    }

    /* private class to allow logger configuration */
    private static class TraceLoggerProxy extends LoggerProxy {
        public TraceLoggerProxy(Logger logger) {
            super(logger);
        }

        @Override
        public void log(Marker marker, String format, Object... arguments) {
            logger.trace(marker, format, arguments);
        }

    }

    /* private class to allow logger configuration */
    private static class InfoLoggerProxy extends LoggerProxy {
        public InfoLoggerProxy(Logger logger) {
            super(logger);
        }

        @Override
        public void log(Marker marker, String format, Object... arguments) {
            logger.info(marker, format, arguments);
        }
    }

    /* private class to allow logger configuration */
    private static class WarnLoggerProxy extends LoggerProxy {
        public WarnLoggerProxy(Logger logger) {
            super(logger);
        }

        @Override
        public void log(Marker marker, String format, Object... arguments) {
            logger.warn(marker, format, arguments);
        }
    }

    /* private class to allow logger configuration */
    private static class ErrorLoggerProxy extends LoggerProxy {
        public ErrorLoggerProxy(Logger logger) {
            super(logger);
        }

        @Override
        public void log(Marker marker, String format, Object... arguments) {
            logger.error(marker, format, arguments);
        }
    }

}
