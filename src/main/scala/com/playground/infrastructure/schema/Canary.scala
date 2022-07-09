package com.playground.infrastructure.schema

import vulcan.Codec
import cats.implicits._

case class Canary(name: String, `type`: String)

object Canary {
  implicit val matchesCodec: Codec[Canary] = Codec.record[Canary](
    name = "Canary",
    namespace = "com.playground"
  )(field =>
    (
      field(
        "name",
        _.name,
      ),
      field(
        "type",
        _.`type`,
      )
    ).mapN(Canary.apply)
  )

}