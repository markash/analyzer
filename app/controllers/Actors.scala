package controllers

import anorm._
import anorm.SqlParser._
import akka.actor.{PoisonPill, ActorSystem, Props, Actor, ActorLogging}
import scala.concurrent.duration._
import play.api.db.DB
import play.api.Play.current

case class Counter(objectName: String, name: String, value: Long)
case class MetricRequest(metric: String, objectName: String, name: String)

class AnalyzerActor extends Actor with ActorLogging {

  val databaseActor = context.actorOf(Props[DatabaseActor], "DatabaseActor")

  def receive = {
    case "collect" => collect()
    case request: MetricRequest => databaseActor.tell(request, sender)
    case "pause" => pause()
    case _ => println("huh?")
  }

  def collect() {
    val jolokia = new Jolokia("L01MarkA", 7001, "system", "password1*")
    try {
      val queues = jolokia.retrieveQueues.map(queue => jolokia.retrieveQueueStatistics(queue))
      queues.foreach(queue => databaseActor ! new Counter(queue.objectName, "messagesCurrentCount", queue.statistics.messagesCurrentCount))
    } catch {
      case ex: java.util.concurrent.ExecutionException => ex.getCause() match { 
        case err: java.net.ConnectException => log.warning("Unable to connect to Jolokia on L01MarkA:7001")
        case err => log.error(err, "Unknown error")
      }
      case ex: java.net.ConnectException => { log.warning("Unable to connect to Jolokia on L01MarkA:7001")}
    }
  }

  def pause() {
    val jolokia = new Jolokia("L01MarkA", 7001, "system", "password1*")
    jolokia.exec("com.bea:JMSServerRuntime=jms-server,Name=jms-module!!ecs.jms.queue.in.accounting,ServerRuntime=testserver,Type=JMSDestinationRuntime", "pauseConsumption")

  }
}

class DatabaseActor extends Actor with ActorLogging {
  val parser = get[String]("objectName") ~ get[String]("name") ~ get[Long]("value") map {
    case objectName~name~value => new Counter(objectName, name, value)
  }

  def receive = {
    case Counter(objectName, name, value) => insertCounter(objectName, name, value)
    case MetricRequest("counter", objectName, name) => sender ! retrieveMetricCounter(objectName, name)
    case _ => log.info("Database does not understand")
  }

  def insertCounter (objectName: String, name: String, value: Long) {
    DB.withConnection { implicit c =>
      SQL("insert into counter (objectName, name, value) values ({objectName}, {name}, {value})")
      .on(
        "objectName" -> objectName, 
        "name" -> name,
        "value" -> value
      ).execute()
    }
  }

  def retrieveMetricCounter(objectName: String, name: String): List[Counter] = {
    DB.withConnection { implicit c =>
      SQL("SELECT objectName, name, value from counter").on("objectName" -> objectName, "name" -> name).as(parser *)
    }
  }
}