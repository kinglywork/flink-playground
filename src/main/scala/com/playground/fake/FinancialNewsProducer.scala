package com.playground.fake

import com.playground.avro.{CodecForSerialize, FlinkAvroSerdes, KafkaRecord}
import com.playground.stock.Config
import com.playground.stock.model.{FinancialNews, PublicTradedCompany}
import org.apache.flink.api.scala.createTypeInformation
import org.apache.flink.streaming.connectors.kafka.KafkaSerializationSchema
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.producer._
import org.apache.kafka.common.serialization.ByteArraySerializer

import java.util.Properties

object FinancialNewsProducer {
  def run(config: Config, companies: List[PublicTradedCompany]): Unit = {
    val industries = companies.map(_.industry).distinct
    val news = FinancialNewsGenerator.generateFinancialNews(industries.size)

    val props = getConfig(config)
    val serializer = getSerializer(config)
    val producer = new KafkaProducer(props, new ByteArraySerializer(), new ByteArraySerializer())
    val callback: Callback = (_: RecordMetadata, e: Exception) => {
      if (e != null) {
        println(e.getMessage)
      }
    }

    var counter = 0
    industries.foreach(industry => {
      val financialNews = FinancialNews(industry, news(counter))
      val record = serializer.serialize(KafkaRecord(industry, financialNews), System.currentTimeMillis())
      producer.send(record, callback)
      counter += 1
    })
  }

  private def getSerializer(config: Config): KafkaSerializationSchema[KafkaRecord[FinancialNews]] = {
    val financialNewsCodec = CodecForSerialize(
      encode = FinancialNews.financialNewsCodec.encode,
      decode = FinancialNews.financialNewsCodec.decode
    )

    new FlinkAvroSerdes[FinancialNews, FinancialNews.financialNewsCodec.Repr](
      config.schemaRegistryUrl
    )
      .serializer(
        config.financialNewsTopic,
        financialNewsCodec
      )
  }

  private def getConfig(config: Config): Properties = {
    val props = new Properties()

    props.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, config.kafkaBootstrapServers)
    props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, config.securityProtocol)

    props
  }
}
