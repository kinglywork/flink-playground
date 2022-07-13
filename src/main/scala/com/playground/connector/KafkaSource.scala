package com.playground.connector

import com.playground.stock.Config
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer, KafkaDeserializationSchema}

import java.util.Properties

object KafkaSource {

  def apply[T: TypeInformation](
    config: Config,
    topic: String,
    deserializer: KafkaDeserializationSchema[T]
  ): FlinkKafkaConsumer[T] = {

    val kafkaConsumer = new FlinkKafkaConsumer(
      topic,
      deserializer,
      makeProperties(config)
    )

    kafkaConsumer.setStartFromEarliest()
    kafkaConsumer
  }

  private def makeProperties(config: Config): Properties = {
    val properties = new Properties()
    properties.setProperty("security.protocol", config.securityProtocol)
    properties.setProperty("bootstrap.servers", config.kafkaBootstrapServers)
    properties.setProperty("group.id", config.appName)
    properties
  }
}
