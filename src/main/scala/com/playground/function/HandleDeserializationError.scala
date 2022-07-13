package com.playground.function

import com.playground.errors.ErrorOr
import io.circe.syntax.EncoderOps
import org.apache.flink.api.common.functions.RichFlatMapFunction
import org.apache.flink.util.Collector
import org.slf4j.LoggerFactory

class HandleDeserializationError[T]() extends RichFlatMapFunction[ErrorOr[T], T] {
  private lazy val LOG = LoggerFactory.getLogger("application")

  override def flatMap(value: ErrorOr[T], out: Collector[T]): Unit = {
    value match {
      case Left(error) =>
        LOG.error(s"Deserialization error report: ${error.asJson.noSpaces}")
      case Right(value) =>
        out.collect(value)
    }
  }
}
