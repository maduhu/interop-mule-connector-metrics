package com.l1p.interop.mule.connector.metrics.reporter;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

public class MetricKafkaProducer {

  private final String servers;
  private Producer<String, String> kafkaProducer;
  private String clientId;

  public MetricKafkaProducer(String servers, String clientId) {
    this.servers = servers;
    this.clientId = clientId;
    init();
  }

  public static void main(String[] args) {
    final MetricKafkaProducer metricKafkaProducer = new MetricKafkaProducer("ec2-52-26-168-223.us-west-2.compute.amazonaws.com:9092", "main-test");

    for (int i = 0; i < 10; i++) {
      metricKafkaProducer.send("test", String.valueOf(i), String.valueOf(i));
    }
    metricKafkaProducer.close();
  }

  private void init() {
    Properties kafkaProps = new Properties();
    kafkaProps.put("client.id", clientId);
    kafkaProps.put("bootstrap.servers", servers);
    kafkaProps.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    kafkaProps.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

    kafkaProducer = new KafkaProducer<>(kafkaProps);
  }

  /**
   * Sends metric to specified topic with metricKey is specified as metric name
   *
   * @param topic
   * @param metricKey
   * @param values
   */
  public void send(String topic, String metricKey, String values) {
    System.out.println("sending " + values);
    kafkaProducer.send(new ProducerRecord<>(topic, metricKey, values));
  }

  public void close() {
    kafkaProducer.close();
  }
}
