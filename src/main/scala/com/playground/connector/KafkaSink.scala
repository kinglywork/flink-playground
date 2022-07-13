package com.playground.connector

import com.playground.avro.KafkaRecord
import com.playground.stock.Config
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer.Semantic
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaProducer, KafkaSerializationSchema}
import org.apache.kafka.clients.CommonClientConfigs

import java.util.Properties

object KafkaSink {

  def makeProperties(config: Config): Properties = {
    val kafkaConfig = new Properties()
    kafkaConfig.setProperty(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, config.kafkaBootstrapServers)
    kafkaConfig.setProperty(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, config.securityProtocol)
    kafkaConfig
  }

  def buildKafkaProducer[T: TypeInformation](
                                              sinkTopic: String,
                                              config: Config,
                                              kafkaSerializer: KafkaSerializationSchema[KafkaRecord[T]]
                                            ): FlinkKafkaProducer[KafkaRecord[T]] = {

    val kafkaConfig = makeProperties(config)

    val producer = new FlinkKafkaProducer(
      sinkTopic,
      kafkaSerializer,
      kafkaConfig,
      Semantic.AT_LEAST_ONCE
    )

    producer.setLogFailuresOnly(true)
    producer
  }
}
