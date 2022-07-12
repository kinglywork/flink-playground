package com.playground.avro

import org.apache.avro.Schema
import org.apache.flink.api.common.typeinfo.TypeInformation
import vulcan.AvroError

/** As vulcan.Codec is not Serializable, the codecs cannot be used directly in the Flink serdes.
  * This class is a wrapper around the encoding and decoding Codec method to be used in Flink.
  */
final case class CodecForSerialize[T: TypeInformation, Repr](
  encode: T => Either[AvroError, Repr],
  decode: (Any, Schema) => Either[AvroError, T]
)
