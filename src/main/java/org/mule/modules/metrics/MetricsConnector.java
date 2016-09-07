package org.mule.modules.metrics;

import static com.codahale.metrics.MetricRegistry.name;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.websocket.DeploymentException;

import org.glassfish.tyrus.server.Server;
import org.mule.api.MuleEvent;
import org.mule.api.annotations.Config;
import org.mule.api.annotations.Connector;
import org.mule.api.annotations.Processor;
import org.mule.api.annotations.lifecycle.Start;
import org.mule.api.annotations.lifecycle.Stop;
import org.mule.api.annotations.param.Default;
import org.mule.api.annotations.param.Optional;
import org.mule.api.transport.PropertyScope;
import org.mule.modules.metrics.config.ConnectorConfig;
import org.mule.modules.metrics.reporter.WebSocket;
import org.mule.modules.metrics.reporter.WebSocketReporter;
import org.mule.modules.metrics.spring.GaugeBean;
import org.mule.modules.metrics.spring.GaugeBeanInterface;
import org.mule.modules.metrics.websocket.MetricsWebSocketEndpoint;
import org.mule.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.Timer;

@Connector(name = "metrics", friendlyName = "Metrics", minMuleVersion = "3.7")
public class MetricsConnector {

	private static Logger log = LoggerFactory.getLogger(MetricsConnector.class);

	@Config
	ConnectorConfig config;

	private Server server;

	@Start
	public void startConnector() {

		/* ==================================================== */
		/* Star WebServer */
		/* ==================================================== */

		if (config.isServerembedded()) {

			/* ==================================================== */
			/* Start WebServer */
			/* ==================================================== */

			server = new Server(config.getHost(), config.getPort(), "/"
					+ config.getServicename(), null,
					MetricsWebSocketEndpoint.class);

			try {
				server.start();
			} catch (DeploymentException e) {
				log.error("Failed to start websocket server", e);
			}

		}

		try {
			Thread.sleep(5000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		/* ==================================================== */
		/* Add WebSocket Reporter */
		/* ==================================================== */

		if (!config.getHost().isEmpty()) {

			final WebSocket websocket = new WebSocket(config.getHost(),
					config.getPort(), config.getServicename(),
					config.isServerembedded());

			final WebSocketReporter reporter = WebSocketReporter
					.forRegistry(config.getMetricRegistry())
					.convertRatesTo(TimeUnit.SECONDS)
					.convertDurationsTo(TimeUnit.MILLISECONDS)
					.filter(MetricFilter.ALL).build(websocket);

			config.getScheduledReporters().add(reporter);

		}

		/* ==================================================== */
		/* Star Reporters */
		/* ==================================================== */

		for (ScheduledReporter scheduledReporter : config
				.getScheduledReporters()) {
			try {
				scheduledReporter.start(config.getReporterInterval(),
						TimeUnit.SECONDS);
			} catch (Exception e) {
				log.error("Failed to start metric report", e);
			}
		}

	}

	@Stop
	public void stopConnector() {

		/* ==================================================== */
		/* Stop Reporters */
		/* ==================================================== */

		for (ScheduledReporter scheduledReporter : config
				.getScheduledReporters()) {
			try {
				scheduledReporter.stop();
			} catch (Exception e) {
				log.error("Failed to stop metric report", e);
			}
		}

		/* ==================================================== */
		/* Stop WebServer */
		/* ==================================================== */

		if (config.isServerembedded()) {

			server.stop();
		}

	}

	@Processor(friendlyName = "Dashboard")
	public void dashboardBuilder(MuleEvent event) throws IOException,
			URISyntaxException {

		String contents = IOUtils.toString(Thread.currentThread()
				.getContextClassLoader().getResourceAsStream("dashboard.html"),
				"UTF-8");

		String uristr = "ws://" + config.getHost() + ":"
				+ Integer.toString(config.getPort()) + "/"
				+ config.getServicename() + "/websockets";

		event.getMessage().setPayload(contents.replace("${service}", uristr));

		event.getMessage().setProperty("Content-Type", "text/html",
				PropertyScope.OUTBOUND);

	}

	@Processor(friendlyName = "meter")
	public void meter(String category, List<String> meterKeys,
			@Optional @Default(value = "1") long count) {
		for (String meterKey : meterKeys) {

			final Meter meter = config.getMetricRegistry().meter(
					name(category, meterKey));

			meter.mark(count);

		}
	}

	@Processor(friendlyName = "snap time")
	public void snapTime(MuleEvent event, String snapTimeKey) {
		event.setSessionVariable(snapTimeKey, System.nanoTime());
	}

	@Processor(friendlyName = "record time measurement")
	public void time(MuleEvent event,
			@Optional @Default(value = "") String category, String metricKey,
			String beginSnapKey, String endSnapKey) {
		final long begin = event.getSessionVariable(beginSnapKey);
		final long end = event.getSessionVariable(endSnapKey);
		final Timer timer = config.getMetricRegistry().timer(
				name(category, metricKey));
		timer.update(end - begin, TimeUnit.NANOSECONDS);
	}

	/**
	 * Increments counters specified by metricKeys
	 *
	 * @param metricKeys
	 *            keys for metrics that need to be incremented
	 */
	@Processor(friendlyName = "increment count")
	public void incCount(String category, List<String> metricKeys,
			@Optional @Default(value = "1") int delta) {
		for (String metricKey : metricKeys) {
			config.getMetricRegistry().counter(name(category, metricKey))
					.inc(delta);
		}
	}

	/**
	 * Decrement counters specified by metricKeys
	 *
	 * @param metricKeys
	 *            keys for metrics that need to be incremented
	 */
	@Processor(friendlyName = "decrement count")
	public void decCount(String category, List<String> metricKeys,
			@Optional @Default(value = "1") int delta) {
		for (String metricKey : metricKeys) {
			config.getMetricRegistry().counter(name(category, metricKey))
					.inc(delta);
		}
	}

	/**
	 * Decrement counters specified by metricKeys
	 *
	 * @param metricKeys
	 *            keys for metrics that need to be incremented
	 */
	@Processor(friendlyName = "Add Gauge")
	public void addGauge(final MuleEvent event, @Optional List<String> gaugeList) {

		if (gaugeList == null || gaugeList.isEmpty()) {

			for (final GaugeBean gaugeBean : event.getMuleContext()
					.getRegistry().lookupObjects(GaugeBean.class)) {

				log.info("Gauge " + gaugeBean.getCategory() + "."
						+ gaugeBean.getName() + " registered");

				if (!config
						.getMetricRegistry()
						.getGauges()
						.containsKey(
								name(gaugeBean.getCategory(),
										gaugeBean.getName()))) {
					config.getMetricRegistry().register(

					name(gaugeBean.getCategory(), gaugeBean.getName()),
							new Gauge<Integer>() {
								@Override
								public Integer getValue() {

									return gaugeBean.returnValue();

								}

							});

				}

			}

		} else {

			for (String gaugeId : gaugeList) {

				final GaugeBeanInterface beanId = event.getMuleContext()
						.getRegistry().lookupObject(gaugeId);

				if (beanId != null) {

					if (!config
							.getMetricRegistry()
							.getGauges()
							.containsKey(
									name(beanId.getCategory(), beanId.getName()))) {
						config.getMetricRegistry().register(

						name(beanId.getCategory(), beanId.getName()),
								new Gauge<Integer>() {
									@Override
									public Integer getValue() {

										return beanId.returnValue();

									}

								});
					}

				}

			}

		}

	}

	public ConnectorConfig getConfig() {
		return config;
	}

	public void setConfig(ConnectorConfig config) {
		this.config = config;
	}

	public Server getServer() {
		return server;
	}

}