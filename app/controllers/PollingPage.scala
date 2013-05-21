package controllers

import akka.actor.{PoisonPill, ActorSystem, Props, Actor}
import scala.concurrent.duration._

object Main extends App {
  import system.dispatcher

  val system = ActorSystem("HelloSystem")
  val helloActor = system.actorOf(Props[AnalyzerActor], name="helloactor")

  println("Requesting...")
//  system.scheduler.scheduleOnce(10 milliseconds, helloActor, "pause")
  system.scheduler.scheduleOnce(20 milliseconds, helloActor, "collect")
  system.scheduler.scheduleOnce(300 milliseconds, helloActor, "bye")

  system.scheduler.scheduleOnce(
    500 milliseconds,
    new Runnable() {
      def run() { system.shutdown()}
    })
}