package com.playground.model.elasticsearch

final case class DocumentId(value: String) extends AnyVal

object DocumentId {
  import io.circe.{Decoder, Encoder}

  implicit val idEncoder: Encoder[DocumentId] = Encoder.encodeString.contramap(_.value)
  implicit val idDecoder: Decoder[DocumentId] = Decoder.decodeString.map(DocumentId(_))
}
