package com.playground.stock.model

import java.util.Date

final case class StockTransaction(
                                   symbol: String,
                                   sector: String,
                                   industry: String,
                                   shares: Int,
                                   sharePrice: Double,
                                   customerId: String,
                                   transactionTimestamp: Date,
                                   purchase: Boolean,
                                 )