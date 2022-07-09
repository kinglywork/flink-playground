package com.playground

import org.apache.flink.api.scala.createTypeInformation
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment

class AppRuntime(config: Config) {
  def start(): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    val wordStream =  env.fromElements("hello world this is Roy")

    wordStream.print()

    env.execute(config.appName)
  }
}
