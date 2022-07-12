package com.playground

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
}
