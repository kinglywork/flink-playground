package com.playground.fake

import com.github.javafaker.Faker
import com.playground.stock.model.{Customer, Purchase, Store}

import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern
import scala.collection.mutable.ListBuffer
import scala.util.Random

object PurchaseDataGenerator {
  def generatePurchases(number: Int, numberCustomers: Int): List[Purchase] = {
    val NUMBER_UNIQUE_STORES: Int = 15;
    val faker = new Faker()
    val customers = generateCustomers(numberCustomers)
    val stores = generateStores(NUMBER_UNIQUE_STORES)

    val random = new Random()

    val purchases = (0 until number).map(_ => {
      val itemPurchased = faker.commerce.productName
      val quantity = faker.number.numberBetween(1, 5)
      val price = faker.commerce.price(4.00, 295.00).toDouble
      val purchaseDate = faker.date.past(15, TimeUnit.MINUTES, new Date())
      val customer = customers(random.nextInt(numberCustomers))
      val store = stores(random.nextInt(NUMBER_UNIQUE_STORES))
      Purchase(
        firstName = customer.firstName,
        lastName = customer.lastName,
        customerId = customer.customerId,
        creditCardNumber = customer.creditCardNumber,
        itemPurchased = itemPurchased,
        department = store.department,
        employeeId = store.employeeId,
        storeId = store.storeId,
        zipCode = store.zipCode,
        quantity = quantity,
        price = price,
        purchaseDate = purchaseDate,
      )
    }).toList

    val electronicsPurchases = purchases.filter(purchase => purchase.department.equalsIgnoreCase("electronics"))
    val coffeePurchase = electronicsPurchases.map(purchase => generateCafePurchase(purchase, faker))

    Random.shuffle(purchases ::: coffeePurchase)
  }

  private def generateCafePurchase(purchase: Purchase, faker: Faker) = {
    val date = purchase.purchaseDate
    val adjusted = date.toInstant.minus(faker.number.numberBetween(8, 18), ChronoUnit.MINUTES);
    val cafeDate = Date.from(adjusted);

    purchase.copy(
      department = "Coffee",
      itemPurchased = faker.options.option("Mocha", "Mild Roast", "Red-Eye", "Dark Roast"),
      price = faker.commerce.price(3.00, 6.00).toDouble,
      quantity = 1,
      purchaseDate = cafeDate
    )
  }

  private def generateStores(numberOfStores: Int): List[Store] = {
    val faker = new Faker
    (0 until numberOfStores)
      .map(i =>
        Store(
          faker.number.randomNumber(5, false).toString,
          faker.options.option("47197-9482", "97666", "113469", "334457"),
          faker.number.randomNumber(6, true).toString,
          if (i % 5 == 0) "Electronics" else faker.commerce.department))
      .toList
  }

  def generateCustomers(numberCustomers: Int): List[Customer] = {
    val faker = new Faker()
    val creditCards = generateCreditCardNumbers(numberCustomers)
    (0 until numberCustomers).map(i => {
      val name = faker.name
      val creditCard = creditCards(i)
      val customerId = faker.idNumber.valid
      Customer(name.firstName, name.lastName, customerId, creditCard)
    }).toList
  }

  private def generateCreditCardNumbers(numberCards: Int): ListBuffer[String] = {
    var counter = 0
    val visaMasterCardAmex = Pattern.compile("(\\d{4}-){3}\\d{4}")
    val creditCardNumbers = ListBuffer[String]()
    val finance = new Faker().finance
    while (counter < numberCards) {
      val cardNumber = finance.creditCard
      if (visaMasterCardAmex.matcher(cardNumber).matches) {
        creditCardNumbers.append(cardNumber)
        counter += 1
      }
    }
    creditCardNumbers
  }
}
