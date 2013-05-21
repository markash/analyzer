package util

import play.api._
import akka.actor.{ActorSystem, Props, Actor}
import scala.concurrent.duration._
import controllers.{AnalyzerActor}

object Global extends GlobalSettings {
  val system = ActorSystem("AnalyzerSystem")
  val analyzerActor = system.actorOf(Props[AnalyzerActor], name="AnalyzerActor")

  import system.dispatcher

  override def onStart(app: Application) {
    Logger.info("Application has started")
    system.scheduler.schedule(20 milliseconds, 5000 milliseconds, analyzerActor, "collect")
  }  
  
  override def onStop(app: Application) {
    Logger.info("Application shutdown...")
    system.shutdown();
  } 
}