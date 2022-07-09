package com.playground

import com.playground.errors.AppError
import org.slf4j.LoggerFactory

object Main {
  private lazy val LOG = LoggerFactory.getLogger("application")

  def main(args: Array[String]): Unit = {
    Config.fromEnvironment match {
      case Left(error) => handleConfigError(error)
      case Right(config) => runWithConfig(config)
    }
  }

  def runWithConfig(config: Config): Unit = {
    LOG.info(s"Main: starting ${config.appName}")

    val appRuntime = new AppRuntime(config)
    appRuntime.start()
  }

  private def handleConfigError(appError: AppError): Unit = {
    LOG.error(s"Main: Failed to start application", appError)
  }
}
