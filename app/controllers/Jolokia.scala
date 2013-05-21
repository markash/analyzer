package controllers

import dispatch._, Defaults._
import play.api.libs.json.{Json, JsValue}
import com.ning.http.client.RequestBuilder

case class QueueStatus(paused: Boolean,
                       consumptionPaused: Boolean,
                       productionPaused: Boolean,
                       insertionPaused: Boolean,
                       insertionPausedState: String,
                       consumptionPausedState: String,
                       productionPausedState: String)
case class QueueStatistics(
                            destinationType: String,
                            messagesCurrentCount: Long,
                            messagesPendingCount: Long,
                            messagesHighCount: Long,
                            messagesDeletedCurrentCount: Long,
                            messagesMovedCurrentCount: Long,
                            messagesReceivedCount: Long,
                            messagesThresholdTime: Long,
                            consumersCurrentCount: Long,
                            consumersHighCount: Long,
                            consumersTotalCount: Long,
                            bytesCurrentCount: Long,
                            bytesPendingCount: Long,
                            bytesHighCount: Long,
                            bytesThresholdTime: Long,
                            bytesReceivedCount: Long
                            )
case class Queue(name: String, objectName: String, status: QueueStatus, statistics: QueueStatistics)

class Jolokia(host: String, port: Int, user: String, password: String) {

  def baseUrl(): String = {
    "http://" + host + ":" + port
  }

  def serviceUrl(operation: String, query: String): RequestBuilder = operation match {
    case "search" => :/(host, port) / "jolokia" / operation / query
    case "exec"   => :/(host, port) / "jolokia" / operation / query as(user, password)
    case _        => :/(host, port) / "jolokia" / operation / query
  }

  def exec(objectName: String, operation: String) = {
    val results = Http(:/(host, port) / "jolokia" / "exec" / objectName / operation as (user, password) OK as.String)
    results()
    val json: JsValue = Json.parse(results())
    println(json)
  }

  def retrieveQueues: Seq[Queue] = {
    val results = Http(serviceUrl("search", "com.bea:*,Type=JMSDestinationRuntime") OK as.String)
    var queues = Seq[Queue]()

    results()
    if (results.isCompleted) {
      val json: JsValue = Json.parse(results())
      val values = (json \ "value").as[List[String]]
      queues = values.map(queue => queue.split(",")).map(fields => new Queue(fields(1).split("!")(1), fields.reduce((x, y) => x + "," + y ), null, null))
    }
    queues
  }

  def retrieveQueueStatistics(queue: Queue): Queue = {
    val results = Http(serviceUrl("read", queue.objectName.replace("!", "!!")) OK as.String)

    results()
    val json: JsValue = Json.parse(results())

    new Queue(
      queue.name,
      queue.objectName,
      new QueueStatus(
        (json \ "value" \ "Paused").as[Boolean],
        (json \ "value" \ "ConsumptionPaused").as[Boolean],
        (json \ "value" \ "ProductionPaused").as[Boolean],
        (json \ "value" \ "InsertionPaused").as[Boolean],
        (json \ "value" \ "InsertionPausedState").as[String],
        (json \ "value" \ "ConsumptionPausedState").as[String],
        (json \ "value" \ "ProductionPausedState").as[String]
      ),
      new QueueStatistics(
        (json \ "value" \ "DestinationType").as[String],
        (json \ "value" \ "MessagesCurrentCount").as[Long],
        (json \ "value" \ "MessagesPendingCount").as[Long],
        (json \ "value" \ "MessagesHighCount").as[Long],
        (json \ "value" \ "MessagesDeletedCurrentCount").as[Long],
        (json \ "value" \ "MessagesMovedCurrentCount").as[Long],
        (json \ "value" \ "MessagesReceivedCount").as[Long],
        (json \ "value" \ "MessagesThresholdTime").as[Long],
        (json \ "value" \ "ConsumersCurrentCount").as[Long],
        (json \ "value" \ "ConsumersHighCount").as[Long],
        (json \ "value" \ "ConsumersTotalCount").as[Long],
        (json \ "value" \ "BytesCurrentCount").as[Long],
        (json \ "value" \ "BytesPendingCount").as[Long],
        (json \ "value" \ "BytesHighCount").as[Long],
        (json \ "value" \ "BytesThresholdTime").as[Long],
        (json \ "value" \ "BytesReceivedCount").as[Long])
    )
  }
}
