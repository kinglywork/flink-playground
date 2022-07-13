package com.playground.function

import org.apache.flink.api.common.functions.FlatMapFunction
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.streaming.api.datastream.DataStreamSink
import org.apache.flink.streaming.api.functions.sink.SinkFunction
import org.apache.flink.streaming.api.scala.{ConnectedStreams, DataStream, KeyedStream}

object StreamExtensions {
  implicit class DataStreamExtensions[T](dataStream: DataStream[T]) {
    def nameAndUid(name: String): DataStream[T] =
      dataStream.name(name).uid(name)

    def pipeWith[U](transformer: DataStream[T] => DataStream[U]): DataStream[U] =
      transformer(dataStream)

    def namedFlatMapWith[U: TypeInformation](name: String)(function: T => TraversableOnce[U]): DataStream[U] =
      dataStream.flatMap(function).nameAndUid(name)

    def namedFlatMap[U: TypeInformation](name: String)(function: FlatMapFunction[T, U]): DataStream[U] =
      dataStream.flatMap(function).nameAndUid(name)

    def namedCollect[U: TypeInformation](name: String)(partialFunction: PartialFunction[T, U]): DataStream[U] =
      dataStream.namedFlatMapWith[U](name)(element => partialFunction.lift(element).toList)

    def namedMap[U: TypeInformation](name: String)(function: T => U): DataStream[U] =
      dataStream.map(function).nameAndUid(name)

    def namedFilter(name: String)(filterFunction: T => Boolean): DataStream[T] =
      dataStream.filter(filterFunction).nameAndUid(name)

    def addNamedSink(name: String)(sink: SinkFunction[T]): DataStreamSink[T] =
      dataStream.addSink(sink).name(name).uid(name)
  }

  implicit class KeyedStreamExtensions[T, K](keyedStream: KeyedStream[T, K]) {
    def namedFilterWithState[S: TypeInformation](name: String)(
      function: (T, Option[S]) => (Boolean, Option[S])
    ): DataStream[T] =
      keyedStream.filterWithState(function).nameAndUid(name)

    def namedReduce(name: String)(function: (T, T) => T): DataStream[T] =
      keyedStream.reduce(function).nameAndUid(name)
  }

  implicit class ConnectedStreamsExtensions[A, B](connectedStreams: ConnectedStreams[A, B]) {
    def namedMap[R: TypeInformation](name: String)(leftMapFunction: A => R, rightMapFunction: B => R): DataStream[R] =
      connectedStreams.map(leftMapFunction, rightMapFunction).nameAndUid(name)
  }
}
