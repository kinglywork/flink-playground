package com.playground.stock

import com.playground.fake.StockTransactionProducer


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

    StockTransactionProducer.run(config, 15, 50, 25)

//    Main.runWithConfig(config)
  }
}
