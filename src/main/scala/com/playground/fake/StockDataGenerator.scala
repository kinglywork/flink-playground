package com.playground.fake

import com.github.javafaker.Faker
import com.playground.stock.model.PublicTradedCompany

import scala.util.Random

object StockDataGenerator {
  def generatePublicTradedCompanies(numberCompanies: Int): List[PublicTradedCompany] = {
    val faker = new Faker()
    val random = new Random()

    (0 until numberCompanies).map(_ => {
      val name = faker.company().name()
      val stripped = name.replaceAll("[^A-Za-z]", "")
      val start = random.nextInt(stripped.length() - 4)
      val symbol = stripped.substring(start, start + 4)
      val volatility = faker.options().option("0.01", "0.02", "0.03", "0.04", "0.05", "0.06", "0.07", "0.08", "0.09").toDouble
      val lastSold = faker.number().randomDouble(2, 15, 150)
      val sector = faker.options().option("Energy", "Finance", "Technology", "Transportation", "Health Care")
      val industry = faker.options().option("Oil & Gas Production", "Coal Mining", "Commercial Banks", "Finance/Investors Services", "Computer Communications Equipment", "Software Consulting", "Aerospace", "Railroads", "Major Pharmaceuticals")

      PublicTradedCompany(
        volatility = volatility,
        lastSold = lastSold,
        symbol = symbol,
        name = name,
        sector = sector,
        industry = industry,
        price = lastSold,
      )
    }).toList
  }
}
