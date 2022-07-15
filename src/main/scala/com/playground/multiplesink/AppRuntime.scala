package com.playground.multiplesink

import com.playground.avro.{CodecForDeserialize, FlinkAvroSerdes, KafkaRecord, TombstoneOr}
import com.playground.connector.{ElasticsearchDocumentSink, KafkaSource}
import com.playground.errors.ErrorOr
import com.playground.function.HandleDeserializationError
import com.playground.model.elasticsearch.{DocumentId, DocumentIndexAction, UpsertIndexAction}
import com.playground.stock.model.StockTransaction
import org.apache.flink.api.scala.createTypeInformation
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.streaming.connectors.elasticsearch7.ElasticsearchSink
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer
import org.apache.http.HttpHost

import java.time.{OffsetDateTime, ZoneOffset}

class AppRuntime(config: Config) {
  def start(): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    val stockStream = env.addSource(createStockTransactionSource())
      .flatMap(new HandleDeserializationError[KafkaRecord[TombstoneOr[StockTransaction]]]())
      .flatMap(record => {
        record match {
          case KafkaRecord(_, Some(stockTransaction)) => Vector(stockTransaction)
          case _ => Vector()
        }
      })

    val stockToDocIndexAction = (stockTransaction: StockTransaction) =>
      UpsertIndexAction(
        id = DocumentId(s"${stockTransaction.customerId}-${stockTransaction.symbol}-${stockTransaction.transactionTimestamp.toEpochSecond(ZoneOffset.UTC)}"),
        payload = stockTransaction,
        processedAt = OffsetDateTime.now())

    val techAndFinanceSector = Vector("Finance", "Technology")

    val techAndFinanceStockStream: DataStream[DocumentIndexAction] = stockStream
      .filter(stockTransaction => techAndFinanceSector.contains(stockTransaction.sector))
      .map(stockToDocIndexAction)
    val otherStockStream: DataStream[DocumentIndexAction] = stockStream
      .filter(stockTransaction => !techAndFinanceSector.contains(stockTransaction.sector))
      .map(stockToDocIndexAction)

    techAndFinanceStockStream.addSink(createEsSink("tech-finance-stock-transaction", config))

    otherStockStream.addSink(createEsSink("other-stock-transaction", config))

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

  private def createEsSink(esIndexName: String, config: Config): ElasticsearchSink[DocumentIndexAction] = {
    ElasticsearchDocumentSink[StockTransaction](
      new HttpHost(config.esHostName, config.esHostPort, config.esHostSchemaName),
      esIndexName,
      config.esFlushMaxActions
    )
  }
}
