package com.playground.fake

import com.playground.avro.{CodecForSerialize, FlinkAvroSerdes, KafkaRecord}
import com.playground.stock.Config
import com.playground.stock.model.StockTransaction
import org.apache.flink.api.scala.createTypeInformation
import org.apache.flink.streaming.connectors.kafka.KafkaSerializationSchema
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.producer._
import org.apache.kafka.common.serialization.ByteArraySerializer

import java.util.Properties

object StockTransactionProducer {
  val DEFAULT_TRANSACTION_COUNT: Int = 50

  def run(config: Config, numberIterations: Int, numberTradedCompanies: Int, numberCustomers: Int): Unit = {
    val companies = StockDataGenerator.generatePublicTradedCompanies(numberTradedCompanies)
    val customers = PurchaseDataGenerator.generateCustomers(numberCustomers)

    val props = getConfig(config)
    val serializer = getSerializer(config)
    val producer = new KafkaProducer(props, new ByteArraySerializer(), new ByteArraySerializer())

    val callback: Callback = (_: RecordMetadata, e: Exception) => {
      if (e != null) {
        println(e.getMessage)
      }
    }

    FinancialNewsProducer.run(config, companies)

    var counter = 0
    while (counter < numberIterations) {
      val transactions = StockTransactionGenerator.generateStockTransactions(customers, companies, DEFAULT_TRANSACTION_COUNT)

      transactions.foreach(transaction => {
        val record = serializer.serialize(KafkaRecord("", transaction), System.currentTimeMillis())
        producer.send(record, callback)
      })

      counter += 1
      Thread.sleep(3000)
    }
  }

  private def getSerializer(config: Config): KafkaSerializationSchema[KafkaRecord[StockTransaction]] = {
    val stockTransactionCodec = CodecForSerialize(
      encode = StockTransaction.stockTransactionCodec.encode,
      decode = StockTransaction.stockTransactionCodec.decode
    )

    new FlinkAvroSerdes[StockTransaction, StockTransaction.stockTransactionCodec.Repr](
      config.schemaRegistryUrl
    )
      .serializer(
        config.stockTransactionTopic,
        stockTransactionCodec
      )
  }

  private def getConfig(config: Config): Properties = {
    val props = new Properties()

    props.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, config.kafkaBootstrapServers)
    props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, config.securityProtocol)

    props
  }
}
