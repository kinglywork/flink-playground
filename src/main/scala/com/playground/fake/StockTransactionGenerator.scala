package com.playground.fake

import com.github.javafaker.Faker
import com.playground.stock.model.{Customer, PublicTradedCompany, StockTransaction}

import java.time.{LocalDateTime, ZoneOffset}
import java.util.Date
import java.util.concurrent.TimeUnit

object StockTransactionGenerator {
  def generateStockTransactions(customers: List[Customer], companies: List[PublicTradedCompany], number: Int): List[StockTransaction] = {
    val faker = new Faker()
    (0 until number).map(_ => {
      val numberShares = faker.number().numberBetween(100, 50000)
      val customer = customers(faker.number().numberBetween(0, customers.size))
      val company = companies(faker.number().numberBetween(0, companies.size))
      val transactionDate = faker.date.past(1, TimeUnit.HOURS, new Date())

      StockTransaction(
        symbol = company.symbol,
        customerId = customer.customerId,
        transactionTimestamp = LocalDateTime.ofInstant(transactionDate.toInstant, ZoneOffset.ofHours(8)),
        sector = company.sector,
        sharePrice = company.updateStockPrice(),
        shares = numberShares,
        industry = company.industry,
        purchase = true,
      )
    }).toList
  }
}
