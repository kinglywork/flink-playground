package com.playground

import vulcan.Codec

import java.time.{LocalDate, LocalDateTime}

package object avro {
  type TombstoneOr[T] = Option[T]

  object TombstoneOr {
    def apply[T](t: T): TombstoneOr[T] = {
      Option(t)
    }

    def empty[T](): TombstoneOr[T] = {
      None
    }
  }

  implicit val localDateCodec: Codec[LocalDate] =
    Codec[String].imap(LocalDate.parse)(_.toString)
  implicit val localDateTimeCodec: Codec[LocalDateTime] =
    Codec[String].imap(LocalDateTime.parse)(_.toString)
}
