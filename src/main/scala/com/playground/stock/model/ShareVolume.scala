package com.playground.stock.model

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class ShareVolume(
                              symbol: String,
                              shares: Int,
                              industry: String,
                            )

object ShareVolume {
  implicit val encoder: Encoder[ShareVolume] = deriveEncoder[ShareVolume]
  implicit val decoder: Decoder[ShareVolume] = deriveDecoder[ShareVolume]

  def apply(stockTransaction: StockTransaction): ShareVolume =
    new ShareVolume(stockTransaction.symbol, stockTransaction.shares, stockTransaction.industry)

  def sum(v1: ShareVolume, v2: ShareVolume): ShareVolume = v1.copy(shares = v1.shares + v2.shares)
}

