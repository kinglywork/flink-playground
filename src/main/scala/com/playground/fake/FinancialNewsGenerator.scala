package com.playground.fake

import com.github.javafaker.Faker

object FinancialNewsGenerator {
  def generateFinancialNews(count: Int): List[String] = {
    val faker = new Faker()
    (0 until count)
      .map(_ => {
        faker.company().bs()
      })
      .toList
  }
}
