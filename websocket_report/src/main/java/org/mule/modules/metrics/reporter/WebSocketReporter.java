package org.mule.modules.metrics.reporter;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

import org.mule.modules.metrics.model.MetricsValueCounter;
import org.mule.modules.metrics.model.MetricsValueGauge;
import org.mule.modules.metrics.model.MetricsValueHistogram;
import org.mule.modules.metrics.model.MetricsValueMetered;
import org.mule.modules.metrics.model.MetricsValueTimer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Clock;
import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Metered;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Timer;

/**
 * A reporter which publishes metric values to throw websocket.
 *
 */
public class WebSocketReporter extends ScheduledReporter {
	/**
	 * Returns a new {@link Builder} for {@link WebSocketReporter}.
	 *
	 * @param registry
	 *            the registry to report
	 * @return a {@link Builder} instance for a {@link WebSocketReporter}
	 */
	public static Builder forRegistry(MetricRegistry registry) {
		return new Builder(registry);
	}

	/**
	 * A builder for {@link WebSocketReporter} instances. Defaults to not using
	 * a prefix, using the default clock, converting rates to events/second,
	 * converting durations to milliseconds, and not filtering metrics.
	 */
	public static class Builder {
		private final MetricRegistry registry;
		private Clock clock;
		private String prefix;
		private TimeUnit rateUnit;
		private TimeUnit durationUnit;
		private MetricFilter filter;

		private Builder(MetricRegistry registry) {
			this.registry = registry;
			this.clock = Clock.defaultClock();
			this.prefix = null;
			this.rateUnit = TimeUnit.SECONDS;
			this.durationUnit = TimeUnit.MILLISECONDS;
			this.filter = MetricFilter.ALL;
		}

		/**
		 * Use the given {@link Clock} instance for the time.
		 *
		 * @param clock
		 *            a {@link Clock} instance
		 * @return {@code this}
		 */
		public Builder withClock(Clock clock) {
			this.clock = clock;
			return this;
		}

		/**
		 * Prefix all metric names with the given string.
		 *
		 * @param prefix
		 *            the prefix for all metric names
		 * @return {@code this}
		 */
		public Builder prefixedWith(String prefix) {
			this.prefix = prefix;
			return this;
		}

		/**
		 * Convert rates to the given time unit.
		 *
		 * @param rateUnit
		 *            a unit of time
		 * @return {@code this}
		 */
		public Builder convertRatesTo(TimeUnit rateUnit) {
			this.rateUnit = rateUnit;
			return this;
		}

		/**
		 * Convert durations to the given time unit.
		 *
		 * @param durationUnit
		 *            a unit of time
		 * @return {@code this}
		 */
		public Builder convertDurationsTo(TimeUnit durationUnit) {
			this.durationUnit = durationUnit;
			return this;
		}

		/**
		 * Only report metrics which match the given filter.
		 *
		 * @param filter
		 *            a {@link MetricFilter}
		 * @return {@code this}
		 */
		public Builder filter(MetricFilter filter) {
			this.filter = filter;
			return this;
		}

		/**
		 * Builds a {@link WebSocketReporter} with the given properties, sending
		 * metrics using the given {@link WebSocketSender}.
		 *
		 * Present for binary compatibility
		 *
		 * @param websocket
		 *            a {@link WebSocket}
		 * @return a {@link WebSocketReporter}
		 */
		public WebSocketReporter build(WebSocket websocket) {
			return build((WebSocketSender) websocket);
		}

		/**
		 * Builds a {@link WebSocketReporter} with the given properties, sending
		 * metrics using the given {@link WebSocketSender}.
		 *
		 * @param websocket
		 *            a {@link WebSocketSender}
		 * @return a {@link WebSocketReporter}
		 */
		public WebSocketReporter build(WebSocketSender websocket) {
			return new WebSocketReporter(registry, websocket, clock, prefix,
					rateUnit, durationUnit, filter);
		}
	}

	private static final Logger LOGGER = LoggerFactory
			.getLogger(WebSocketReporter.class);

	private final WebSocketSender websocket;
	private final Clock clock;
	private final String prefix;

	private WebSocketReporter(MetricRegistry registry,
			WebSocketSender websocket, Clock clock, String prefix,
			TimeUnit rateUnit, TimeUnit durationUnit, MetricFilter filter) {
		super(registry, "websocket-reporter", filter, rateUnit, durationUnit);
		this.websocket = websocket;
		this.clock = clock;
		this.prefix = prefix;
	}

	@Override
	public void report(SortedMap<String, Gauge> gauges,
			SortedMap<String, Counter> counters,
			SortedMap<String, Histogram> histograms,
			SortedMap<String, Meter> meters, SortedMap<String, Timer> timers) {
		final long timestamp = clock.getTime() / 1000;

		try {

			if (!websocket.isConnected()) {
				websocket.connect();
			}

			for (Map.Entry<String, Gauge> entry : gauges.entrySet()) {
				reportGauge(entry.getKey(), entry.getValue(), timestamp);
			}

			for (Map.Entry<String, Counter> entry : counters.entrySet()) {
				reportCounter(entry.getKey(), entry.getValue(), timestamp);
			}

			for (Map.Entry<String, Histogram> entry : histograms.entrySet()) {
				reportHistogram(entry.getKey(), entry.getValue(), timestamp);
			}

			for (Map.Entry<String, Meter> entry : meters.entrySet()) {
				reportMetered(entry.getKey(), entry.getValue(), timestamp);
			}

			for (Map.Entry<String, Timer> entry : timers.entrySet()) {
				reportTimer(entry.getKey(), entry.getValue(), timestamp);
			}

			websocket.flush();

		} catch (IOException e) {
			LOGGER.warn("Unable to report to WebSocket", websocket, e);
			try {
				websocket.close();
			} catch (IOException e1) {
				LOGGER.warn("Error closing WebSocket", websocket, e1);
			}
		}
	}

	@Override
	public void stop() {
		try {
			super.stop();
		} finally {
			try {
				websocket.close();
			} catch (IOException e) {
				LOGGER.debug("Error disconnecting from WebSocket", websocket, e);
			}
		}
	}

	private void reportTimer(String name, Timer timer, long timestamp)
			throws IOException {

		final Snapshot snapshot = timer.getSnapshot();

		MetricsValueTimer timerValue = new MetricsValueTimer(name, timestamp);

		timerValue.setMax(format(convertDuration(snapshot.getMax())));
		timerValue.setMean(format(convertDuration(snapshot.getMean())));
		timerValue.setMin(format(convertDuration(snapshot.getMin())));
		timerValue.setStddev(format(convertDuration(snapshot.getStdDev())));
		timerValue.setP50(format(convertDuration(snapshot.getMedian())));
		timerValue
				.setP75(format(convertDuration(snapshot.get75thPercentile())));
		timerValue
				.setP95(format(convertDuration(snapshot.get95thPercentile())));
		timerValue
				.setP98(format(convertDuration(snapshot.get98thPercentile())));
		timerValue
				.setP99(format(convertDuration(snapshot.get99thPercentile())));
		timerValue
				.setP999(format(convertDuration(snapshot.get999thPercentile())));

		websocket.send(timerValue);

	}

	private void reportMetered(String name, Metered meter, long timestamp)
			throws IOException {

		MetricsValueMetered meteredValue = new MetricsValueMetered(name,
				timestamp);

		meteredValue.setCount(format(meter.getCount()));
		meteredValue.setM1_rate(format(meter.getOneMinuteRate()));
		meteredValue.setM5_rate(format(meter.getFiveMinuteRate()));
		meteredValue.setM15_rate(format(meter.getFifteenMinuteRate()));
		meteredValue.setMean_rate(format(meter.getMeanRate()));

		websocket.send(meteredValue);

	}

	private void reportHistogram(String name, Histogram histogram,
			long timestamp) throws IOException {

		final Snapshot snapshot = histogram.getSnapshot();

		MetricsValueHistogram histogramValue = new MetricsValueHistogram(name,
				timestamp);

		histogramValue.setCount(format(histogram.getCount()));
		histogramValue.setMax(format(snapshot.getMax()));
		histogramValue.setMean(format(snapshot.getMean()));
		histogramValue.setMin(format(snapshot.getMin()));
		histogramValue.setStddev(format(snapshot.getStdDev()));
		histogramValue.setP50(format(snapshot.getMedian()));
		histogramValue.setP75(format(snapshot.get75thPercentile()));
		histogramValue.setP95(format(snapshot.get95thPercentile()));
		histogramValue.setP98(format(snapshot.get98thPercentile()));
		histogramValue.setP99(format(snapshot.get99thPercentile()));
		histogramValue.setP999(format(snapshot.get999thPercentile()));

		websocket.send(histogramValue);

	}

	private void reportCounter(String name, Counter counter, long timestamp)
			throws IOException {

		MetricsValueCounter counterValue = new MetricsValueCounter(name,
				timestamp, format(counter.getCount()));

		websocket.send(counterValue);

	}

	private void reportGauge(String name, Gauge gauge, long timestamp)
			throws IOException {
		final String value = format(gauge.getValue());

		if (value != null) {

			MetricsValueGauge counterValue = new MetricsValueGauge(name,
					timestamp, value);

			websocket.send(counterValue);
		}
	}

	private String format(Object o) {
		if (o instanceof Float) {
			return format(((Float) o).doubleValue());
		} else if (o instanceof Double) {
			return format(((Double) o).doubleValue());
		} else if (o instanceof Byte) {
			return format(((Byte) o).longValue());
		} else if (o instanceof Short) {
			return format(((Short) o).longValue());
		} else if (o instanceof Integer) {
			return format(((Integer) o).longValue());
		} else if (o instanceof Long) {
			return format(((Long) o).longValue());
		} else if (o instanceof BigInteger) {
			return format(((BigInteger) o).doubleValue());
		} else if (o instanceof BigDecimal) {
			return format(((BigDecimal) o).doubleValue());
		}
		return null;
	}

	private String prefix(String... components) {
		return MetricRegistry.name(prefix, components);
	}

	private String format(long n) {
		return Long.toString(n);
	}

	private String format(double v) {
		return String.format(Locale.US, "%2.2f", v);
	}
}