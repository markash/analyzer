package controllers

import play.api.mvc.{Controller, Action}
import scala.concurrent.Await
import scala.concurrent.duration._
import akka.actor.{Actor, ActorRef}
import akka.util.Timeout
import akka.pattern.ask
import util.{Global}

object QueuesPage extends Controller with ApplicationMenu {

  def statistics = Action {
    val settings = Settings.retrieve().getOrElse(Settings.default)
    val jolokia = new Jolokia(settings.host, settings.port, "system", "password1*")
    val queues = jolokia.retrieveQueues.map(queue => jolokia.retrieveQueueStatistics(queue))

    implicit val timeout = Timeout(5 seconds)
    val objectName = "com.bea:JMSServerRuntime=jms-server,Name=jms-module!!ecs.jms.queue.in.accounting,ServerRuntime=testserver,Type=JMSDestinationRuntime"
    val future = Global.analyzerActor ? new MetricRequest("counter", objectName, "messagesCurrentCount")
    val result = Await.result(future, timeout.duration).asInstanceOf[List[Counter]]
   	result.foreach(counter => println(counter))

    Ok(views.html.statistics(sections)(queues))
  }
}
