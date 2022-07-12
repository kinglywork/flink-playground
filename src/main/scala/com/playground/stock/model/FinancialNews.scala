package com.playground.stock.model

import cats.implicits.catsSyntaxTuple2Semigroupal
import vulcan.Codec

final case class FinancialNews(industry: String, content: String)

object FinancialNews {
  implicit val financialNewsCodec: Codec[FinancialNews] = Codec.record[FinancialNews](
    name = "FinancialNews",
    namespace = "com.playground"
  )(field =>
    (
      field(
        "industry",
        _.industry
      ),
      field(
        "content",
        _.content
      )
    ).mapN(FinancialNews.apply)
  )
}