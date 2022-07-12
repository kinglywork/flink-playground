package com.playground.stock

import cats.implicits.toBifunctorOps
import com.amazonaws.services.kinesisanalytics.runtime.KinesisAnalyticsRuntime
import com.playground.errors.{AppError, ConfigError, ErrorOr}
import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder

import java.net.URI
import scala.util.Try

final case class Config(
                         appName: String,
                         version: String,
                         schemaRegistryUrl: String,
                         kafkaBootstrapServers: String,
                         securityProtocol: String,
                         stockTransactionTopic: String,
                         financialNewsTopic: String
                       )

object Config {
  implicit val configEncoder: Encoder[Config] = deriveEncoder
  implicit private[Config] val urlEncoder: Encoder[URI] = Encoder.encodeString.contramap(_.toString)

  private val applicationProperties = KinesisAnalyticsRuntime.getApplicationProperties

  def fromEnvironment: ErrorOr[Config] = for {
    appName <- lookup("APP_NAME")
    version <- lookup("APP_VERSION")
    schemaRegistryUrl <- lookup("SCHEMA_REGISTRY_URL")
    kafkaBootstrapServers <- lookup("KAFKA_BOOTSTRAP_SERVERS")
    securityProtocol <- lookup("SECURITY_PROTOCOL")
    stockTransactionTopic <- lookup("STOCK_TRANSACTION_TOPIC")
    stockTransactionTopic <- lookup("FINANCIAL_NEWS_TOPIC")
  } yield Config(
    appName,
    version,
    schemaRegistryUrl,
    kafkaBootstrapServers,
    securityProtocol,
    stockTransactionTopic,
    stockTransactionTopic
  )

  private def lookup(name: String): ErrorOr[String] =
    sys.env.get(name).orElse(lookupApplicationProperties(name)) match {
      case Some(value: String) => Right[AppError, String](value)
      case Some(_) =>
        Left[AppError, String](ConfigError(name, None, Some(new Error("Config value is not a String"))))
      case None => Left[AppError, String](ConfigError(name, None, Some(new Error("Cannot find config value"))))
    }

  private def lookupApplicationProperties(name: String): Option[AnyRef] =
    for {
      properties <- Option(applicationProperties.get("environmentProperties"))
      value <- Option(properties.get(name))
    } yield value

  private def lookupAndParse[A](key: String)(parse: String => A): ErrorOr[A] = {
    lookup(key).flatMap { value => parseValue(key, value)(parse) }
  }

  private def parseValue[A](key: String, value: String)(parse: String => A): ErrorOr[A] = {
    Try(parse(value)).toEither.leftMap(err => ConfigError(key, Some(value), Some(err)))
  }
}
