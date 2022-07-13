package com.playground.stock.model

import org.apache.flink.api.common.state.{ListState, ListStateDescriptor}
import org.apache.flink.streaming.api.functions.KeyedProcessFunction
import org.apache.flink.util.Collector
import scala.collection.convert.ImplicitConversions.`iterable AsScalaIterable`
import scala.collection.JavaConverters._

class TopStock(maxSize: Int) extends KeyedProcessFunction[String, ShareVolume, Vector[ShareVolume]] {
  lazy val state: ListState[ShareVolume] = getRuntimeContext
    .getListState(new ListStateDescriptor[ShareVolume]("myState", classOf[ShareVolume]))

  override def processElement(
                               value: ShareVolume,
                               ctx: KeyedProcessFunction[String, ShareVolume, Vector[ShareVolume]]#Context,
                               out: Collector[Vector[ShareVolume]]): Unit = {
    val topN = state.get().toVector
    val deduplicated = topN.filter(_.symbol != value.symbol) :+ value
    val sorted = deduplicated.sortBy(_.shares)(Ordering[Int].reverse)
    val newTopN = sorted.take(maxSize)

    state.update(newTopN.asJava)
    out.collect(newTopN)
  }
}

object TopStock {
  def serializeTopN(shareVolumes: Vector[ShareVolume]): String = {
    val industry = shareVolumes.head.industry
    val symbols = shareVolumes.map(shareVolume => s"${shareVolume.symbol}:${shareVolume.shares}").mkString(", ")

    s"$industry -- $symbols"
  }
}
