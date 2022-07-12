package com.playground.stock.model

import java.text.SimpleDateFormat
import java.util.{Date, Locale}

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder, Json}

final case class Purchase(
                           firstName: String,
                           lastName: String,
                           customerId: String,
                           creditCardNumber: String,
                           itemPurchased: String,
                           department: String,
                           employeeId: String,
                           quantity: Int,
                           price: Double,
                           purchaseDate: Date,
                           zipCode: String,
                           storeId: String
                         )

object Purchase {
  private val dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.ENGLISH)
  implicit val dateEncoder: Encoder[Date] = Encoder.instance(date => Json.fromString(dateFormat.format(date)))
  implicit val dateDecoder: Decoder[Date] = Decoder.instance(hCursor => hCursor.as[String].map(dateFormat.parse))
  implicit val purchaseEncoder: Encoder[Purchase] = deriveEncoder[Purchase]
  implicit val purchaseDecoder: Decoder[Purchase] = deriveDecoder[Purchase]

  private val CC_NUMBER_REPLACEMENT: String = "xxxx-xxxx-xxxx-"

  def markCreditCard(purchase: Purchase): Purchase = {
    val parts: Array[String] = purchase.creditCardNumber.split("-")
    val markedCreditCard = if (parts.length < 4) "xxxx" else CC_NUMBER_REPLACEMENT + parts(3)

    purchase.copy(creditCardNumber = markedCreditCard)
  }
}

