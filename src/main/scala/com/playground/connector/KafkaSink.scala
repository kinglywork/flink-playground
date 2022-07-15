package com.playground.connector

import com.playground.avro.KafkaRecord
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer.Semantic
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaProducer, KafkaSerializationSchema}
import org.apache.kafka.clients.CommonClientConfigs

import java.util.Properties

object KafkaSink {

  def buildKafkaProducer[T: TypeInformation](
    kafkaBootstrapServers: String,
    securityProtocol: String,
    sinkTopic: String,
    kafkaSerializer: KafkaSerializationSchema[KafkaRecord[T]]
  ): FlinkKafkaProducer[KafkaRecord[T]] = {

    val properties = new Properties()
    properties.setProperty(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers)
    properties.setProperty(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, securityProtocol)

    val producer = new FlinkKafkaProducer(
      sinkTopic,
      kafkaSerializer,
      properties,
      Semantic.AT_LEAST_ONCE
    )

    producer.setLogFailuresOnly(true)
    producer
  }
}
