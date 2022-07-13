package com.playground.stock

import cats.implicits.{toBifunctorOps, toTraverseOps}
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
                         financialNewsTopic: String,
                         esHostName: String,
                         esHostPort: Int,
                         esHostSchemaName: String,
                         esIndexName: String,
                         esFlushMaxActions: Option[Int] = Some(1)
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
    financialNewsTopic <- lookup("FINANCIAL_NEWS_TOPIC")
    esHostName <- lookup("ES_HOST_NAME")
    esHostPort <- lookupAndParse("ES_HOST_PORT")(_.toInt)
    esHostSchemaName <- lookup("ES_HOST_SCHEMA_NAME")
    esIndexName <- lookup("ES_INDEX_NAME")
    esFlushMaxActions <- extractOptionFromEnv("ES_FLUSH_MAX_ACTIONS")(_.toInt)
  } yield Config(
    appName,
    version,
    schemaRegistryUrl,
    kafkaBootstrapServers,
    securityProtocol,
    stockTransactionTopic,
    financialNewsTopic,
    esHostName,
    esHostPort,
    esHostSchemaName,
    esIndexName,
    esFlushMaxActions
  )

  private def lookup(name: String): ErrorOr[String] =
    sys.env.get(name).orElse(lookupApplicationProperties(name)) match {
      case Some(value: String) => Right[AppError, String](value)
      case Some(_) =>
        Left[AppError, String](ConfigError(name, None, Some(new Error("Config value is not a String"))))
      case None => Left[AppError, String](ConfigError(name, None, Some(new Error("Cannot find config value"))))
    }

  private def extractOptionFromEnv[A](key: String)(parse: String => A): ErrorOr[Option[A]] = {
    sys.env.get(key).traverse { value => parseValue(key, value)(parse) }
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
