package com.playground.stock.model.elasticsearch

import com.playground.stock.model.ShareVolume

import java.time.OffsetDateTime

sealed trait DocumentIndexAction {
  val id: DocumentId
  val processedAt: OffsetDateTime
}

final case class UpsertIndexAction(
  id: DocumentId,
  processedAt: OffsetDateTime,
  shareVolumes: Vector[ShareVolume]
) extends DocumentIndexAction

final case class DeleteAction(
  id: DocumentId,
  processedAt: OffsetDateTime,
) extends DocumentIndexAction
