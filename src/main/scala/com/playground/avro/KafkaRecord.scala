package com.playground.avro

final case class KafkaRecord[T](key: String, value: T)
