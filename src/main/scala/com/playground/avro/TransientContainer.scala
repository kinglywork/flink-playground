package com.playground.avro

import java.util.Objects.isNull

/** This is a generic container to hold a value that cannot be serialized. The first time `get` is
  * called, the value will be constructed and then stored so that future calls to `get` use the
  * original value instead.
  */
@SuppressWarnings(Array("org.wartremover.warts.Var"))
final case class TransientContainer[T <: AnyRef](construct: () => T) {

  /** Stores the constructed value for future calls to `get`. When the container is deserialized,
    * this will be filled in with `null` as it is marked `@transient`.
    */
  @transient private var nullableInstance: T = construct()

  /** Return the internally stored value. If the container was just deserialized and there is no
    * internally stored value, a new one is constructed before being returned.
    */
  def instance: T = {
    if (isNull(nullableInstance)) {
      nullableInstance = construct()
    }
    nullableInstance
  }
}
