package com.playground.stock.model

final case class TransactionSummary(
                                     customerId: String,
                                     symbol: String,
                                     industry: String,
                                     totalShares: Int,
                                     tradingCount: Int,
                                   )

object TransactionSummary {
  def apply(stockTransaction: StockTransaction): TransactionSummary =
    TransactionSummary(
      stockTransaction.customerId,
      stockTransaction.symbol,
      stockTransaction.industry,
      totalShares = 0,
      tradingCount = 0,
    )
}