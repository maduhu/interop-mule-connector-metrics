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
public class CsvReporterWithDeltas extends ScheduledReporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(CsvReporter.class);
    private static final Charset UTF_8 = Charset.forName("UTF-8");
    private final File directory;
    private final Locale locale;
    private final Clock clock;
    private final MetricRegistry registry;

    private final Map<String, Long> timerCounts = new HashMap<String, Long>();
    long lastTimestamp = 0;
    //private final int intervalLimit = 5;
    //int idleCount = 0;
    //int idleTimer = 0;

    public static CsvReporterWithDeltas.Builder forRegistry(MetricRegistry registry, File directory, String env, String app) {
        return new CsvReporterWithDeltas.Builder(registry, directory, env, app);
    }

    private CsvReporterWithDeltas(MetricRegistry registry, File directory, Locale locale, TimeUnit rateUnit, TimeUnit durationUnit, Clock clock, MetricFilter filter) {
        super(registry, "csv-reporter", filter, rateUnit, durationUnit);
        this.directory = directory;
        this.locale = locale;
        this.clock = clock;
        this.registry = registry;
    }

    public void report(SortedMap<String, Gauge> gauges, SortedMap<String, Counter> counters, SortedMap<String, Histogram> histograms, SortedMap<String, Meter> meters, SortedMap<String, Timer> timers) {
        long timestamp = TimeUnit.MILLISECONDS.toSeconds(this.clock.getTime());
        Iterator var8 = counters.entrySet().iterator();
        Entry entry;

        Timer.Context reportDuration = null;
        try {
            LOGGER.info("begin csv reporting");
            final Timer timer = registry.timer("metrics-csv-reporting-timer");
            final Counter counter = registry.counter("metrics-csv-reporting-counter");
            reportDuration = timer.time();

            if ( counterValueChanged( counters.entrySet() ) ) {
                while(var8.hasNext()) {
                    entry = (Entry)var8.next();
                    this.reportCounter(timestamp, (String)entry.getKey(), (Counter)entry.getValue());
                }
            }

            var8 = timers.entrySet().iterator();
            if ( timerValueChanged( timers.entrySet() ) ) {
                while(var8.hasNext()) {
                    entry = (Entry)var8.next();
                    this.reportTimer(timestamp, (String)entry.getKey(), (Timer)entry.getValue());
                }
            }

            lastTimestamp = timestamp;
            LOGGER.info("end csv reporting");
        } finally {
            if (reportDuration != null) reportDuration.stop();
        }
    }

    /**
     * Method to only report data when counter values have changed
     *
     * @param counters
     * @return
     */
    private boolean counterValueChanged( Set<Entry<String, Counter>> counters ) {

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
         double intervalRate = 0.0d;
         long delta = 0;
         long interval = 0;

         if ( lastTimestamp > 0 ) {
             interval = timestamp - lastTimestamp;
             Long lastSample = timerCounts.get( name );

             if ( interval > 0 && lastSample != null ) {
                 delta = ( totalCount - lastSample );
                 intervalRate = delta  / (double)interval;
             }
         }

         timerCounts.put( name, totalCount );

         this.report(timestamp, name,
         "total_count,interval,delta,interval_rate,min,max,mean,rate_unit,duration_unit",
         "%d,%d,%d,%f,%f,%f,%f,calls/%s,%s",
         timer.getCount(),
         interval,
         delta,
         intervalRate,
         convertDuration(snapshot.getMin()),
         convertDuration(snapshot.getMax()),
         convertDuration(snapshot.getMean()),
         getRateUnit(),
         getDurationUnit());

    }

    private void reportCounter(long timestamp, String name, Counter counter) {
        long totalCount =  counter.getCount();
        long currentCount = 0;
        long elapsed = 0;

        if ( lastTimestamp > 0 ) {
            elapsed = timestamp - lastTimestamp;
            Long lastSample = timerCounts.get( name );

            if ( elapsed > 0 && lastSample != null ) {
                currentCount = ( totalCount - lastSample );
            }
        }
        timerCounts.put( name, totalCount );

        this.report(timestamp, name, "total_count,interval,delta", "%d,%d,%d",
                new Object[]{ Long.valueOf( totalCount ), Long.valueOf( elapsed), Long.valueOf( currentCount ) }  );

    }

    private void report(long timestamp, String name, String header, String line, Object... values) {
        try {
            File e = new File(this.directory, this.sanitize(name) + ".csv");
            boolean fileAlreadyExists = e.exists();
            if(fileAlreadyExists || e.createNewFile()) {
                FileOutputStream fOutStream = new FileOutputStream(e, true);
                OutputStreamWriter oStreamWriter = new OutputStreamWriter (fOutStream, UTF_8);
                PrintWriter out = new PrintWriter(oStreamWriter);
                //PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(e, true), UTF_8));

                try {
                    if(!fileAlreadyExists) {
                        out.println("t," + header);
                    }

                    out.printf(this.locale, String.format(this.locale, "%d,%s%n", new Object[]{Long.valueOf(timestamp), line}), values);
                } finally {
                    out.close();
                    oStreamWriter.close();
                    fOutStream.close();
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
        private File directory;

        private Builder(MetricRegistry registry, File directory, String env, String app) {
            this.registry = registry;
            this.env = env;
            this.app = app;
            this.locale = Locale.getDefault();
            this.rateUnit = TimeUnit.SECONDS;
            this.durationUnit = TimeUnit.MILLISECONDS;
            this.clock = Clock.defaultClock();
            this.filter = MetricFilter.ALL;
            this.directory = directory;
        }

        public CsvReporterWithDeltas.Builder formatFor(Locale locale) {
            this.locale = locale;
            return this;
        }

        public CsvReporterWithDeltas.Builder convertRatesTo(TimeUnit rateUnit) {
            this.rateUnit = rateUnit;
            return this;
        }

        public CsvReporterWithDeltas.Builder convertDurationsTo(TimeUnit durationUnit) {
            this.durationUnit = durationUnit;
            return this;
        }

        public CsvReporterWithDeltas.Builder withClock(Clock clock) {
            this.clock = clock;
            return this;
        }

        public CsvReporterWithDeltas.Builder filter(MetricFilter filter) {
            this.filter = filter;
            return this;
        }

        public CsvReporterWithDeltas build(File directory) {
            //create directory under current project - not currently checking if this fails
            directory.mkdir();
            return new CsvReporterWithDeltas(this.registry, this.directory, this.locale, this.rateUnit, this.durationUnit, this.clock, this.filter);
        }
    }

}
