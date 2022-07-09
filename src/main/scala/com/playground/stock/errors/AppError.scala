package com.playground.stock.errors

import io.circe._
import io.circe.generic.semiauto._
import io.circe.syntax._

import java.io.{PrintWriter, StringWriter}
import scala.util.control.NoStackTrace

sealed trait AppError extends NoStackTrace {
  def message: String
  override def getMessage: String = message
}

final case class ConfigError(configEntry: String, configValue: Option[String], throwable: Option[Throwable])
  extends AppError {
  val message = "Invalid or missing configuration entry"
}

final case class DeserializationError(message: String, throwable: Option[Throwable]) extends AppError
final case class SerializationError(message: String, throwable: Option[Throwable]) extends AppError

object AppError {

  implicit private val configErrorEncoder: Encoder[ConfigError] = deriveEncoder[ConfigError]
  implicit private val deserializationErrorEncoder: Encoder[DeserializationError] = deriveEncoder[DeserializationError]
  implicit private val serializationErrorEncoder: Encoder[SerializationError] = deriveEncoder[SerializationError]

  implicit val appErrorEncoder: Encoder[AppError] = {
    case error: ConfigError =>
      encodeWithMessage(error)
    case error: DeserializationError => encodeWithMessage(error)
    case error: SerializationError => encodeWithMessage(error)
  }

  implicit val throwableEncoder: Encoder[Throwable] = throwable => {
    def getStacktrace: String = {
      val sw = new StringWriter()
      throwable.printStackTrace(new PrintWriter(sw))
      sw.toString
    }
    Json.obj("error" -> Option(throwable.getMessage).asJson, "trace" -> getStacktrace.asJson)
  }

  private def encodeWithMessage[A <: AppError](error: A)(implicit encoder: Encoder[A]): Json = {
    Json.obj("message" -> error.message.asJson, "errorDetail" -> encoder(error))
  }

}
