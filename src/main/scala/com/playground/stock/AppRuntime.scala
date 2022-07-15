package com.playground.stock

import com.playground.avro.{CodecForDeserialize, FlinkAvroSerdes, KafkaRecord, TombstoneOr}
import com.playground.connector.KafkaSource
import com.playground.errors.ErrorOr
import com.playground.function.HandleDeserializationError
import com.playground.stock.model.{ShareVolume, StockTransaction, TopStock}
import org.apache.flink.api.scala.createTypeInformation
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer

class AppRuntime(config: Config) {
  def start(): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    val topNStream = env.addSource(createStockTransactionSource())
      .flatMap(new HandleDeserializationError[KafkaRecord[TombstoneOr[StockTransaction]]]())
      .flatMap(record => {
        record match {
          case KafkaRecord(_, Some(stockTransaction)) => Vector(stockTransaction)
          case _ => Vector()
        }
      })
      .map(ShareVolume(_))
      .keyBy(_.symbol)
      .reduce(ShareVolume.sum _)
      .keyBy(_.industry)
      .process(new TopStock(5))

    topNStream.map(TopStock.serializeTopN _)
      .print()

    val _ = env.execute(config.appName)
  }

  private def createStockTransactionSource(): FlinkKafkaConsumer[ErrorOr[KafkaRecord[TombstoneOr[StockTransaction]]]] = {
    val stockTransactionCodec
    : CodecForDeserialize[StockTransaction, StockTransaction.stockTransactionCodec.Repr] =
      CodecForDeserialize(
        schema = StockTransaction.stockTransactionCodec.schema,
        encode = StockTransaction.stockTransactionCodec.encode,
        decode = StockTransaction.stockTransactionCodec.decode
      )

    val stockTransactionDeserializer =
      new FlinkAvroSerdes[StockTransaction, StockTransaction.stockTransactionCodec.Repr](
        config.schemaRegistryUrl
      ).deserializer(config.stockTransactionTopic, stockTransactionCodec)

    val stockTransactionSource = KafkaSource[ErrorOr[KafkaRecord[TombstoneOr[StockTransaction]]]](
      appName = config.appName,
      kafkaBootstrapServers = config.kafkaBootstrapServers,
      securityProtocol = config.securityProtocol,
      topic = config.stockTransactionTopic,
      deserializer = stockTransactionDeserializer
    )
    stockTransactionSource
  }
}
