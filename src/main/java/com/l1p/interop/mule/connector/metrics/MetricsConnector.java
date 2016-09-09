package com.l1p.interop.mule.connector.metrics;

import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.Timer;
import com.l1p.interop.mule.connector.metrics.config.ConnectorConfig;
import org.mule.api.MuleEvent;
import org.mule.api.annotations.Config;
import org.mule.api.annotations.Connector;
import org.mule.api.annotations.Processor;
import org.mule.api.annotations.lifecycle.Start;
import org.mule.api.annotations.lifecycle.Stop;
import org.mule.api.annotations.param.Default;
import org.mule.api.annotations.param.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.codahale.metrics.MetricRegistry.name;

@Connector(name = "metrics", friendlyName = "Metrics", minMuleVersion = "3.7")
public class MetricsConnector {

  private static Logger log = LoggerFactory.getLogger(MetricsConnector.class);

  @Config
  ConnectorConfig config;

  @Start
  public void startConnector() {
    for (ScheduledReporter scheduledReporter : config.getScheduledReporters()) {
      try {
        scheduledReporter.start(config.getReporterInterval(), TimeUnit.SECONDS);
      } catch (Exception e) {
        log.error("Failed to start metric report", e);
      }
    }

  }

  @Stop
  public void stopConnector() {

    for (ScheduledReporter scheduledReporter : config.getScheduledReporters()) {
      try {
        scheduledReporter.stop();
      } catch (Exception e) {
        log.error("Failed to stop metric report", e);
      }
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
   * @param metricKeys keys for metrics that need to be incremented
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
   * @param metricKeys keys for metrics that need to be incremented
   */
  @Processor(friendlyName = "decrement count")
  public void decCount(String category, List<String> metricKeys,
                       @Optional @Default(value = "1") int delta) {
    for (String metricKey : metricKeys) {
      config.getMetricRegistry().counter(name(category, metricKey))
          .inc(delta);
    }
  }

//  @Processor(friendlyName = "meter")
//  public void meter(String category, List<String> meterKeys, @Optional @Default(value = "1") long count) {
//    for (String meterKey : meterKeys) {
//      final Meter meter = config.getMetricRegistry().meter(name(category, meterKey));
//      meter.mark(count);
//    }
//  }

//  /**
//   *
//   * @param event
//   * @param gaugeList
//   */
//  @Processor(friendlyName = "Add Gauge")
//  public void addGauge(final MuleEvent event, @Optional List<String> gaugeList) {
//
//    if (gaugeList == null || gaugeList.isEmpty()) {
//
//      for (final GaugeBean gaugeBean : event.getMuleContext()
//          .getRegistry().lookupObjects(GaugeBean.class)) {
//
//        log.info("Gauge " + gaugeBean.getCategory() + "."
//            + gaugeBean.getName() + " registered");
//
//        if (!config
//            .getMetricRegistry()
//            .getGauges()
//            .containsKey(
//                name(gaugeBean.getCategory(),
//                    gaugeBean.getName()))) {
//          config.getMetricRegistry().register(
//
//              name(gaugeBean.getCategory(), gaugeBean.getName()),
//              new Gauge<Integer>() {
//                @Override
//                public Integer getValue() {
//
//                  return gaugeBean.returnValue();
//
//                }
//
//              });
//
//        }
//
//      }
//
//    } else {
//
//      for (String gaugeId : gaugeList) {
//
//        final GaugeBeanInterface beanId = event.getMuleContext()
//            .getRegistry().lookupObject(gaugeId);
//
//        if (beanId != null) {
//
//          if (!config
//              .getMetricRegistry()
//              .getGauges()
//              .containsKey(
//                  name(beanId.getCategory(), beanId.getName()))) {
//            config.getMetricRegistry().register(
//
//                name(beanId.getCategory(), beanId.getName()),
//                new Gauge<Integer>() {
//                  @Override
//                  public Integer getValue() {
//
//                    return beanId.returnValue();
//
//                  }
//
//                });
//          }
//
//        }
//
//      }
//
//    }
//
//  }

  public ConnectorConfig getConfig() {
    return config;
  }

  public void setConfig(ConnectorConfig config) {
    this.config = config;
  }
}