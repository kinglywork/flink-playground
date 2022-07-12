package com.playground.stock.model

import java.util.concurrent.ThreadLocalRandom

final case class PublicTradedCompany(
                                      volatility: Double,
                                      lastSold: Double,
                                      symbol: String,
                                      name: String,
                                      sector: String,
                                      industry: String,
                                      var price: Double,
                                    ) {

  def updateStockPrice(): Double = {
    val min = price * -volatility
    val max = price * volatility
    val randomNum = ThreadLocalRandom.current().nextDouble(min, max + 1)
    price = price + randomNum

    price
  }

}
