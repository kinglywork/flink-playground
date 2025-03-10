package com.playground.avro

import cats.implicits.{toBifunctorOps, toTraverseOps}
import com.playground.errors.{DeserializationError, ErrorOr, SerializationError}
import io.confluent.kafka.serializers.{KafkaAvroDeserializer, KafkaAvroSerializer}
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.streaming.api.scala.createTypeInformation
import org.apache.flink.streaming.connectors.kafka.{KafkaDeserializationSchema, KafkaSerializationSchema}
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.Serdes
import vulcan.Codec

import java.{lang, util}
import scala.collection.JavaConverters.mapAsJavaMapConverter
import scala.util.Try

@SuppressWarnings(
  Array("org.wartremover.warts.Throw", "org.wartremover.warts.AsInstanceOf", "org.wartremover.warts.JavaSerializable")
)
class FlinkAvroSerdes[T: TypeInformation, Repr](schemaRegistryUrl: String) extends Serializable {

  private val props: util.Map[String, String] = Map(
    "schema.registry.url" -> schemaRegistryUrl,
    "auto.register.schemas" -> "false"
  ).asJava

  def serializer(
    topic: String,
    serializableCodec: CodecForSerialize[T, Repr]
  ): KafkaSerializationSchema[KafkaRecord[T]] =
    new KafkaSerializationSchema[KafkaRecord[T]] {
      // The KafkaAvroSerializer is not Serializable and therefore must be in a TransientContainer
      private val valueKafkaAvroSerializer = TransientContainer(() => {
        val serializer = new KafkaAvroSerializer()
        serializer.configure(props, false)
        serializer
      })

      override def serialize(
        element: KafkaRecord[T],
        timestamp: lang.Long
      ): ProducerRecord[Array[Byte], Array[Byte]] = {

        val errorOrSerialized = for {
          encoded <- encodeValue(serializableCodec, element)
          serialized <- serializeValue(topic, valueKafkaAvroSerializer.instance, encoded)
          key = Serdes.String().serializer().serialize(topic, element.key)
          producerRecord = new ProducerRecord(topic, key, serialized)
        } yield producerRecord

        errorOrSerialized match {
          case Left(error) => throw error
          case Right(producerRecord) => producerRecord
        }

      }

      private def encodeValue(
                               serializableCodec: CodecForSerialize[T, Repr],
                               element: KafkaRecord[T]
                             ): Either[SerializationError, Repr] = {
        serializableCodec
          .encode(element.value)
          .leftMap(error => SerializationError("Error occurs when encode value", Some(error.throwable)))
      }
      private def serializeValue(
                                  topic: String,
                                  avroSerializer: KafkaAvroSerializer,
                                  encodedValue: Repr
                                ): Either[SerializationError, Array[Byte]] = {
        Try(avroSerializer.serialize(topic, encodedValue)).toEither
          .leftMap(error => SerializationError("Error occurs when serialize value", Some(error)))
      }
    }

  def deserializer(
    topic: String,
    serializableCodec: CodecForDeserialize[T, Repr]
  ): KafkaDeserializationSchema[ErrorOr[KafkaRecord[TombstoneOr[T]]]] =
    new KafkaDeserializationSchema[ErrorOr[KafkaRecord[TombstoneOr[T]]]] {
      // The KafkaAvroDeserializer is not Serializable and therefore must be in a TransientContainer
      private val valueKafkaAvroDeserializer = TransientContainer(() => {
        val deserializer = new KafkaAvroDeserializer()
        deserializer.configure(props, false)
        deserializer
      })

      override def deserialize(
        message: ConsumerRecord[Array[Byte], Array[Byte]]
      ): ErrorOr[KafkaRecord[TombstoneOr[T]]] =
        for {
          recordKey <- Try(Serdes.String().deserializer().deserialize(topic, message.key())).toEither.leftMap(error =>
            DeserializationError(
              deserializationErrorMessage("Error occurs while deserializing key", message),
              Some(error)
            )
          )
          recordValue <- Option(message.value()).traverse(decodeValue(message))
        } yield KafkaRecord(recordKey, recordValue)

      private def decodeValue(
        message: ConsumerRecord[Array[Byte], Array[Byte]]
      ): Array[Byte] => Either[DeserializationError, T] = value => {
        for {
          deserializedValue <- Try(valueKafkaAvroDeserializer.instance.deserialize(topic, value)).toEither
            .leftMap(error =>
              DeserializationError(
                deserializationErrorMessage("Error occurs while deserializing value", message),
                Some(error)
              )
            )
          decodedValue <- Codec
            .decode(deserializedValue)(
              Codec.instance(serializableCodec.schema, serializableCodec.encode, serializableCodec.decode)
            )
            .leftMap(error =>
              DeserializationError(
                deserializationErrorMessage("Error occurs while decode value", message),
                Some(error.throwable)
              )
            )
        } yield decodedValue
      }

      override def isEndOfStream(nextElement: ErrorOr[KafkaRecord[TombstoneOr[T]]]): Boolean = false

      override def getProducedType: TypeInformation[ErrorOr[KafkaRecord[TombstoneOr[T]]]] =
        createTypeInformation[ErrorOr[KafkaRecord[TombstoneOr[T]]]]

      private def deserializationErrorMessage(text: String, consumerRecord: ConsumerRecord[Array[Byte], Array[Byte]]) =
        s"$text, topic: ${consumerRecord.topic()}, partition: ${consumerRecord.partition()} offset: ${consumerRecord.offset()}"
    }
}
