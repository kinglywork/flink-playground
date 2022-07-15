package com.playground.connector

import com.playground.model.elasticsearch.{DeleteAction, DocumentIndexAction, UpsertIndexAction}
import io.circe.Encoder
import io.circe.syntax.EncoderOps
import org.apache.flink.api.common.functions.RuntimeContext
import org.apache.flink.streaming.connectors.elasticsearch.{ActionRequestFailureHandler, ElasticsearchSinkFunction, RequestIndexer}
import org.apache.flink.streaming.connectors.elasticsearch.ElasticsearchSinkBase.FlushBackoffType
import org.apache.flink.streaming.connectors.elasticsearch7.ElasticsearchSink
import org.apache.http.HttpHost
import org.elasticsearch.action.ActionRequest
import org.elasticsearch.action.delete.DeleteRequest
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.client.Requests
import org.elasticsearch.common.xcontent.XContentType
import org.slf4j.LoggerFactory

import java.util

object ElasticsearchDocumentSink {

  private lazy val LOG = LoggerFactory.getLogger("application")
  private val flushBackOffDelayMillis = 2000L

  def apply[T: Encoder](
    host: HttpHost,
    index: String,
    bulkFlushMaxActions: Option[Int]
  ): ElasticsearchSink[DocumentIndexAction] = {
    val esSinkBuilder = new ElasticsearchSink.Builder[DocumentIndexAction](
      makeHostList(host),
      makeSinkFunction(index)
    )
    esSinkBuilder.setFailureHandler(handleFailure)
    esSinkBuilder.setBulkFlushBackoff(true)
    bulkFlushMaxActions.map(esSinkBuilder.setBulkFlushMaxActions)
    esSinkBuilder.setBulkFlushBackoffType(FlushBackoffType.EXPONENTIAL)
    esSinkBuilder.setBulkFlushBackoffDelay(flushBackOffDelayMillis)
    esSinkBuilder.setBulkFlushBackoffRetries(3)
    esSinkBuilder.build()
  }

  @SuppressWarnings(Array("org.wartremover.warts.Throw"))
  private def handleFailure: ActionRequestFailureHandler =
    (action: ActionRequest, failure: Throwable, restStatusCode: Int, indexer: RequestIndexer) => {
      LOG.error(
        s"Error occurs while sinking to elasticsearch. actionRequest: $action, restStatusCode: $restStatusCode",
        failure
      )
      throw failure
    }

  private def makeHostList(host: HttpHost): util.ArrayList[HttpHost] = {
    val httpHosts = new util.ArrayList[HttpHost]
    httpHosts.add(host)
    httpHosts
  }

  private def makeIndexRequest[T: Encoder](
    index: String,
    id: String,
    value: T
  ): IndexRequest = {
    Requests
      .indexRequest(index)
      .id(id)
      .source(value.asJson.noSpaces, XContentType.JSON)
  }

  private def makeDeleteRequest(index: String, id: String): DeleteRequest = Requests.deleteRequest(index).id(id)

  private def makeSinkFunction[T: Encoder](index: String): ElasticsearchSinkFunction[DocumentIndexAction] =
    (element: DocumentIndexAction, _: RuntimeContext, indexer: RequestIndexer) =>
      element match {
        case UpsertIndexAction(id, _, payload) =>
          indexer.add(makeIndexRequest(index, id.value, payload.asInstanceOf[T]))
        case DeleteAction(id, _) =>
          indexer.add(makeDeleteRequest(index, id.value))
      }
}
