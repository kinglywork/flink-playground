package com.playground.stock

import cats.implicits.toBifunctorOps
import com.amazonaws.services.kinesisanalytics.runtime.KinesisAnalyticsRuntime
import com.playground.errors.ErrorOr
import com.playground.stock.errors.{AppError, ConfigError}
import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder

import java.net.URI
import scala.util.Try

final case class Config(
                         appName: String,
                         version: String,
                         schemaRegistryUrl: URI,
                         kafkaBootstrapServers: Vector[URI],
                         securityProtocol: String,
                       )

object Config {
  implicit val configEncoder: Encoder[Config] = deriveEncoder
  implicit private[Config] val urlEncoder: Encoder[URI] = Encoder.encodeString.contramap(_.toString)

  private val applicationProperties = KinesisAnalyticsRuntime.getApplicationProperties

  def fromEnvironment: ErrorOr[Config] = for {
    appName <- lookup("APP_NAME")
    version <- lookup("APP_VERSION")
    schemaRegistryUrl <- lookupAndParse("SCHEMA_REGISTRY_URL")(new URI(_))
    kafkaBootstrapServers <- lookupAndParse("KAFKA_BOOTSTRAP_SERVERS")(value =>
      value.split(",").map(new URI(_)).toVector
    )
    securityProtocol <- lookup("SECURITY_PROTOCOL")
  } yield Config(
    appName,
    version,
    schemaRegistryUrl,
    kafkaBootstrapServers,
    securityProtocol,
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
