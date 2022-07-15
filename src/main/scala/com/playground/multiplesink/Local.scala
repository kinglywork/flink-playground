package com.playground.multiplesink

//import com.playground.fake.StockTransactionProducer
//import scala.concurrent.ExecutionContext.Implicits.global
//import scala.concurrent.Future


object Local {
  def main(args: Array[String]): Unit = {
    val config: Config = Config(
      appName = "sold-transaction-projection-local",
      version = "0.0.1",
      schemaRegistryUrl = "http://localhost:8081",
      kafkaBootstrapServers = "localhost:39092",
      securityProtocol = "PLAINTEXT",
      stockTransactionTopic = "StockTransaction",
      esHostName = "localhost",
      esHostPort = 9200,
      esHostSchemaName = "http",
      esIndexName = "stock-share-volume-top-n-by-industry",
      esFlushMaxActions = Some(1)
    )

//    Future {
//      StockTransactionProducer.run(config, 50, 50, 25)
//    }

    Main.runWithConfig(config)
  }
}
