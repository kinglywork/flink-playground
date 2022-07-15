package com.playground.connector

import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer, KafkaDeserializationSchema}

import java.util.Properties

object KafkaSource {

  def apply[T: TypeInformation](
     appName: String,
     kafkaBootstrapServers: String,
     securityProtocol: String,
     topic: String,
     deserializer: KafkaDeserializationSchema[T]
   ): FlinkKafkaConsumer[T] = {

    val properties = new Properties()
    properties.setProperty("security.protocol", securityProtocol)
    properties.setProperty("bootstrap.servers", kafkaBootstrapServers)
    properties.setProperty("group.id", appName)

    val kafkaConsumer = new FlinkKafkaConsumer(
      topic,
      deserializer,
      properties
    )

    kafkaConsumer.setStartFromEarliest()
    kafkaConsumer
  }
}
