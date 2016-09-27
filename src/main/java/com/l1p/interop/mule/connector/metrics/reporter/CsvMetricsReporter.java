package com.l1p.interop.mule.connector.metrics.reporter;

import com.codahale.metrics.*;
import com.codahale.metrics.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

/**
 * A reporter class for logging metrics values to a CSV file similar to {@link CsvReporter}
 * or {@link ConsoleReporter}.
 */
public class CsvMetricsReporter extends ScheduledReporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(CsvReporter.class);
    private static final Charset UTF_8 = Charset.forName("UTF-8");
    private final File directory;
    private final Locale locale;
    private final Clock clock;

    private final Map<String, Long> timerCounts = new HashMap<String, Long>();
    long lastTimestamp = 0;
    long lastTimestampForCount = 0;
    private final int intervalLimit = 5;
    int idleCount = 0;
    int idleTimer = 0;

    public static CsvMetricsReporter.Builder forRegistry(MetricRegistry registry, String csvTopic, File directory, String env, String app) {
        return new CsvMetricsReporter.Builder(registry, csvTopic, directory, env, app);
    }

    private CsvMetricsReporter(MetricRegistry registry, File directory, Locale locale, TimeUnit rateUnit, TimeUnit durationUnit, Clock clock, MetricFilter filter) {
        super(registry, "csv-reporter", filter, rateUnit, durationUnit);
        this.directory = directory;
        this.locale = locale;
        this.clock = clock;
    }

    public void report(SortedMap<String, Gauge> gauges, SortedMap<String, Counter> counters, SortedMap<String, Histogram> histograms, SortedMap<String, Meter> meters, SortedMap<String, Timer> timers) {
        long timestamp = TimeUnit.MILLISECONDS.toSeconds(this.clock.getTime());
        Iterator var8 = counters.entrySet().iterator();

        Entry entry;
        /*
        while(var8.hasNext()) {
            entry = (Entry)var8.next();
            this.reportGauge(timestamp, (String)entry.getKey(), (Gauge)entry.getValue());
        }
        */

        int counter = 0;
        if ( valueChanged( counters.entrySet() ) ) {
            while(var8.hasNext()) {
                entry = (Entry)var8.next();
                this.reportCounter(timestamp, (String)entry.getKey(), (Counter)entry.getValue());
            }
        }

        /*
        var8 = histograms.entrySet().iterator();
        while(var8.hasNext()) {
            entry = (Entry)var8.next();
            this.reportHistogram(timestamp, (String)entry.getKey(), (Histogram)entry.getValue());
        }

        var8 = meters.entrySet().iterator();
        while(var8.hasNext()) {
            entry = (Entry)var8.next();
            this.reportMeter(timestamp, (String)entry.getKey(), (Meter)entry.getValue());
        }
        */

        var8 = timers.entrySet().iterator();
        if ( timerValueChanged( timers.entrySet() ) ) {
            while(var8.hasNext()) {
                entry = (Entry)var8.next();
                this.reportTimer(timestamp, (String)entry.getKey(), (Timer)entry.getValue());
            }
        }

    }

    /**
     * Method to only report data when counter values have changed
     *
     * @param counters
     * @return
     */
    private boolean valueChanged( Set<Entry<String, Counter>> counters ) {

        for (Entry<String, Counter> entry : counters) {
            String name = entry.getKey();
            Counter nextCounter = entry.getValue();
            Long lastSample = timerCounts.get( name );

            if ( lastSample == null || ( lastSample.compareTo( nextCounter.getCount() ) != 0 ) )  {
                return true;
            }
        }

        return false;
    }

    /**
     * Method to only report data when timer values have changed
     *
     * @param timers
     * @return
     */
    private boolean timerValueChanged( Set<Entry<String, Timer>> timers ) {
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

    private void reportTimer(long timestamp, String name, Timer timer) {
         Snapshot snapshot = timer.getSnapshot();

         Long totalCount = timer.getCount();
         double rate = 0.0d;
         long count = totalCount;
         long elapsed = timestamp;

         if ( lastTimestamp > 0 ) {
             elapsed = timestamp - lastTimestamp;
             Long lastSample = timerCounts.get( name );

             if ( elapsed > 0 && lastSample != null ) {
                 count = ( totalCount - lastSample );
                 rate = count / (double)elapsed;
             }
         }

         lastTimestamp = timestamp;
         timerCounts.put( name, totalCount );

        //this.report(timestamp, name, "count,max,mean,min,stddev,p50,p75,p95,p98,p99,p999,mean_rate,m1_rate,m5_rate,m15_rate,rate_unit,duration_unit", "%d,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,calls/%s,%s", new Object[]{Long.valueOf(timer.getCount()), Double.valueOf(this.convertDuration((double)snapshot.getMax())), Double.valueOf(this.convertDuration(snapshot.getMean())), Double.valueOf(this.convertDuration((double)snapshot.getMin())), Double.valueOf(this.convertDuration(snapshot.getStdDev())), Double.valueOf(this.convertDuration(snapshot.getMedian())), Double.valueOf(this.convertDuration(snapshot.get75thPercentile())), Double.valueOf(this.convertDuration(snapshot.get95thPercentile())), Double.valueOf(this.convertDuration(snapshot.get98thPercentile())), Double.valueOf(this.convertDuration(snapshot.get99thPercentile())), Double.valueOf(this.convertDuration(snapshot.get999thPercentile())), Double.valueOf(this.convertRate(timer.getMeanRate())), Double.valueOf(this.convertRate(timer.getOneMinuteRate())), Double.valueOf(this.convertRate(timer.getFiveMinuteRate())), Double.valueOf(this.convertRate(timer.getFifteenMinuteRate())), this.getRateUnit(), this.getDurationUnit()});
         this.report(timestamp,
         name,
         "timePeriod,delta,totalCount,currentRate,min,max,mean,rate_unit,duration_unit",
         //"timePeriod,delta,totalCount,currentRate,min,max,mean,p75,p95,p98,p99,p999,mean_rate,m1_rate,m5_rate,m15_rate,rate_unit,duration_unit",
         "%d,%d,%d,%f,%f,%f,%f,calls/%s,%s",
         elapsed,
         count,
         timer.getCount(),
         rate,
         convertDuration(snapshot.getMin()),
         convertDuration(snapshot.getMax()),
         convertDuration(snapshot.getMean()),
         //convertDuration(snapshot.get75thPercentile()),
         //convertDuration(snapshot.get95thPercentile()),
         //convertDuration(snapshot.get98thPercentile()),
         //convertDuration(snapshot.get99thPercentile()),
         //convertDuration(snapshot.get999thPercentile()),
         //convertRate(timer.getOneMinuteRate()),
         //convertRate(timer.getFiveMinuteRate()),
         //convertRate(timer.getFifteenMinuteRate()),
         getRateUnit(),
         getDurationUnit());

    }

    private void reportMeter(long timestamp, String name, Meter meter) {
        this.report(timestamp, name, "count,mean_rate,m1_rate,m5_rate,m15_rate,rate_unit", "%d,%f,%f,%f,%f,events/%s", new Object[]{Long.valueOf(meter.getCount()), Double.valueOf(this.convertRate(meter.getMeanRate())), Double.valueOf(this.convertRate(meter.getOneMinuteRate())), Double.valueOf(this.convertRate(meter.getFiveMinuteRate())), Double.valueOf(this.convertRate(meter.getFifteenMinuteRate())), this.getRateUnit()});
    }

    private void reportHistogram(long timestamp, String name, Histogram histogram) {
        Snapshot snapshot = histogram.getSnapshot();
        this.report(timestamp, name, "count,max,mean,min,stddev,p50,p75,p95,p98,p99,p999", "%d,%d,%f,%d,%f,%f,%f,%f,%f,%f,%f", new Object[]{Long.valueOf(histogram.getCount()), Long.valueOf(snapshot.getMax()), Double.valueOf(snapshot.getMean()), Long.valueOf(snapshot.getMin()), Double.valueOf(snapshot.getStdDev()), Double.valueOf(snapshot.getMedian()), Double.valueOf(snapshot.get75thPercentile()), Double.valueOf(snapshot.get95thPercentile()), Double.valueOf(snapshot.get98thPercentile()), Double.valueOf(snapshot.get99thPercentile()), Double.valueOf(snapshot.get999thPercentile())});
    }

    private void reportCounter(long timestamp, String name, Counter counter) {
        long totalCount =  counter.getCount();
        long count = totalCount;
        long elapsed = timestamp;

        if ( lastTimestampForCount > 0 ) {
            elapsed = timestamp - lastTimestampForCount;
            Long lastSample = timerCounts.get( name );

            if ( elapsed > 0 && lastSample != null ) {
                count = ( totalCount - lastSample );
            }
        }
        timerCounts.put( name, totalCount );
        lastTimestampForCount = timestamp;

        this.report(elapsed, name, "count", "%d", new Object[]{Long.valueOf(count)});
        //this.report(timestamp, name, "count", "%d", new Object[]{Long.valueOf(counter.getCount())});

    }

    private void reportGauge(long timestamp, String name, Gauge gauge) {
        this.report(timestamp, name, "value", "%s", new Object[]{gauge.getValue()});
    }

    private void report(long timestamp, String name, String header, String line, Object... values) {
        try {
            File e = new File(this.directory, this.sanitize(name) + ".csv");
            boolean fileAlreadyExists = e.exists();
            if(fileAlreadyExists || e.createNewFile()) {
                PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(e, true), UTF_8));

                try {
                    if(!fileAlreadyExists) {
                        out.println("t," + header);
                    }

                    out.printf(this.locale, String.format(this.locale, "%d,%s%n", new Object[]{Long.valueOf(timestamp), line}), values);
                } finally {
                    out.close();
                }
            }
        } catch (IOException var14) {
            LOGGER.warn("Error writing to {}", name, var14);
        }

    }

    protected String sanitize(String name) {
        return name;
    }

    public static class Builder {
        private final MetricRegistry registry;
        private final String env;
        private final String app;
        private Locale locale;
        private TimeUnit rateUnit;
        private TimeUnit durationUnit;
        private Clock clock;
        private MetricFilter filter;
        private String csvTopic;
        private File directory;

        private Builder(MetricRegistry registry, String csvTopic, File directory, String env, String app) {
            this.registry = registry;
            this.env = env;
            this.app = app;
            this.locale = Locale.getDefault();
            this.rateUnit = TimeUnit.SECONDS;
            this.durationUnit = TimeUnit.MILLISECONDS;
            this.clock = Clock.defaultClock();
            this.filter = MetricFilter.ALL;
            this.csvTopic = csvTopic;
            this.directory = directory;
        }

        public CsvMetricsReporter.Builder formatFor(Locale locale) {
            this.locale = locale;
            return this;
        }

        public CsvMetricsReporter.Builder convertRatesTo(TimeUnit rateUnit) {
            this.rateUnit = rateUnit;
            return this;
        }

        public CsvMetricsReporter.Builder convertDurationsTo(TimeUnit durationUnit) {
            this.durationUnit = durationUnit;
            return this;
        }

        public CsvMetricsReporter.Builder withClock(Clock clock) {
            this.clock = clock;
            return this;
        }

        public CsvMetricsReporter.Builder filter(MetricFilter filter) {
            this.filter = filter;
            return this;
        }

        public CsvMetricsReporter build(File directory) {
            return new CsvMetricsReporter(this.registry, this.directory, this.locale, this.rateUnit, this.durationUnit, this.clock, this.filter);
        }
    }

}
