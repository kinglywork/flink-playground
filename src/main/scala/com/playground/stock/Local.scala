package com.playground.stock

import java.net.URI

object Local {
  def main(args: Array[String]): Unit = {
    val config: Config = Config(
      appName = "sold-transaction-projection-local",
      version = "0.0.1",
      schemaRegistryUrl = new URI("http://localhost:8081"),
      kafkaBootstrapServers = Vector(
        new URI("localhost:39092")
      ),
      securityProtocol = "PLAINTEXT",
    )

    Main.runWithConfig(config)
  }
}
