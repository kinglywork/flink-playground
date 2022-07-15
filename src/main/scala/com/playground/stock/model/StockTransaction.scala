package com.playground.stock.model

import vulcan.Codec
import cats.implicits._

import java.time.LocalDateTime
import com.playground.avro._
import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder

final case class StockTransaction(
                                   symbol: String,
                                   sector: String,
                                   industry: String,
                                   shares: Int,
                                   sharePrice: Double,
                                   customerId: String,
                                   transactionTimestamp: LocalDateTime,
                                   purchase: Boolean,
                                 )

object StockTransaction {
  implicit val stockTransactionCodec: Codec[StockTransaction] = Codec.record[StockTransaction](
    name = "StockTransaction",
    namespace = "com.playground"
  )(field =>
    (
      field(
        "symbol",
        _.symbol
      ),
      field(
        "sector",
        _.sector
      ),
      field(
        "industry",
        _.industry,
      ),
      field(
        "shares",
        _.shares,
      ),
      field(
        "sharePrice",
        _.sharePrice,
      ),
      field(
        "customerId",
        _.customerId,
      ),
      field(
        "transactionTimestamp",
        _.transactionTimestamp,
      ),
      field(
        "purchase",
        _.purchase,
      )
      ).mapN(StockTransaction.apply)
  )

  implicit val encoder: Encoder[StockTransaction] = deriveEncoder[StockTransaction]
}

