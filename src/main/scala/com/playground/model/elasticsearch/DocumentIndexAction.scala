package com.playground.model.elasticsearch

import io.circe.Encoder

import java.time.OffsetDateTime

sealed trait DocumentIndexAction {
  val id: DocumentId
  val processedAt: OffsetDateTime
}

final case class UpsertIndexAction[T: Encoder](
  id: DocumentId,
  processedAt: OffsetDateTime,
  payload: T
) extends DocumentIndexAction

final case class DeleteAction(
  id: DocumentId,
  processedAt: OffsetDateTime,
) extends DocumentIndexAction
