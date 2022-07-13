package com.playground.stock

import com.playground.fake.StockTransactionProducer

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


object Local {
  def main(args: Array[String]): Unit = {
    val config: Config = Config(
      appName = "sold-transaction-projection-local",
      version = "0.0.1",
      schemaRegistryUrl = "http://localhost:8081",
      kafkaBootstrapServers = "localhost:39092",
      securityProtocol = "PLAINTEXT",
      stockTransactionTopic = "StockTransaction",
      financialNewsTopic = "FinancialNews"
    )

    Future {
      StockTransactionProducer.run(config, 50, 50, 25)
    }

    Main.runWithConfig(config)
  }
}
