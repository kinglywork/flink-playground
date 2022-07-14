package com.playground.stock

import com.playground.avro.{CodecForDeserialize, FlinkAvroSerdes, KafkaRecord, TombstoneOr}
import com.playground.connector.KafkaSource
import com.playground.errors.ErrorOr
import com.playground.function.HandleDeserializationError
import com.playground.stock.model.{FinancialNews, StockTransaction, TransactionSummary, TransactionSummaryAndFinancialNews}
import org.apache.flink.api.common.functions.AggregateFunction
import org.apache.flink.api.java.functions.KeySelector
import org.apache.flink.api.scala.createTypeInformation
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.streaming.api.windowing.assigners.SlidingProcessingTimeWindows
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer

class AppRuntime2(config: Config) {
  def start(): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    val transactionSummaryStream = env.addSource(createStockTransactionSource())
      .flatMap(new HandleDeserializationError[KafkaRecord[TombstoneOr[StockTransaction]]]())
      .flatMap(record => {
        record match {
          case KafkaRecord(_, Some(stockTransaction)) => Vector(stockTransaction)
          case _ => Vector()
        }
      })
      .keyBy(new KeySelector[StockTransaction, TransactionSummary]() {
        override def getKey(stockTransaction: StockTransaction): TransactionSummary =
          TransactionSummary(stockTransaction)
      })
      .window(SlidingProcessingTimeWindows.of(Time.seconds(10), Time.seconds(5)))
      .aggregate(new AggregateFunction[StockTransaction, TransactionSummary, TransactionSummary]() {
        override def createAccumulator(): TransactionSummary = TransactionSummary("", "", "", 0, 0)

        override def add(stockTransaction: StockTransaction, accumulator: TransactionSummary): TransactionSummary =
          TransactionSummary(stockTransaction).copy(
            totalShares = stockTransaction.shares + accumulator.totalShares,
            tradingCount = accumulator.tradingCount + 1
          )

        override def getResult(accumulator: TransactionSummary): TransactionSummary = accumulator

        override def merge(a: TransactionSummary, b: TransactionSummary): TransactionSummary =
          a.copy(
            totalShares = a.totalShares + b.totalShares,
            tradingCount = a.tradingCount + b.tradingCount
          )
      })

    val financialNewsStream = env.addSource(createFinancialNewsSource())
      .flatMap(new HandleDeserializationError[KafkaRecord[TombstoneOr[FinancialNews]]]())
      .flatMap(record => {
        record match {
          case KafkaRecord(_, Some(financialNews)) => Vector(financialNews)
          case _ => Vector()
        }
      })

    transactionSummaryStream
      .connect(financialNewsStream)
      .map(TransactionSummaryAndFinancialNews.fromTransactionSummary, TransactionSummaryAndFinancialNews.fromFinancialNews)
      .keyBy(_.industry)
      .reduce(TransactionSummaryAndFinancialNews.aggregate _)
      .flatMap(transactionAndNews => {
        (transactionAndNews.maybeTransactionSummary, transactionAndNews.maybeFinancialNews) match {
          case (Some(transaction), Some(news)) => Vector(s"${transaction.customerId} purchased ${transaction.totalShares} related to news: ${news.content}")
          case _ => Vector()
        }
      })
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
      config = config,
      topic = config.stockTransactionTopic,
      deserializer = stockTransactionDeserializer
    )
    stockTransactionSource
  }

  private def createFinancialNewsSource(): FlinkKafkaConsumer[ErrorOr[KafkaRecord[TombstoneOr[FinancialNews]]]] = {
    val financialNewsCodec
    : CodecForDeserialize[FinancialNews, FinancialNews.financialNewsCodec.Repr] =
      CodecForDeserialize(
        schema = FinancialNews.financialNewsCodec.schema,
        encode = FinancialNews.financialNewsCodec.encode,
        decode = FinancialNews.financialNewsCodec.decode
      )

    val financialNewsDeserializer =
      new FlinkAvroSerdes[FinancialNews, FinancialNews.financialNewsCodec.Repr](
        config.schemaRegistryUrl
      ).deserializer(config.financialNewsTopic, financialNewsCodec)

    val financialNewsSource = KafkaSource[ErrorOr[KafkaRecord[TombstoneOr[FinancialNews]]]](
      config = config,
      topic = config.financialNewsTopic,
      deserializer = financialNewsDeserializer
    )
    financialNewsSource
  }
}
