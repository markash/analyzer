package controllers

import play.api._
import play.api.mvc._

case class Section(name: String, items: Seq[MenuItem])
case class MenuItem(name: String, url: String)


object Global extends GlobalSettings {
  override def onStart(app: Application) {
    Logger.info("Creating database tables...")
    //Settings.create
  }

  override def onStop(app: Application) {
    Logger.info("Application stopping")
  }
}

trait ApplicationMenu {
  def sections = List(
    new Section(
      "Queues",
      List(
        new MenuItem("Home", "/"),
        new MenuItem("Settings", "/settings"),
        new MenuItem("Statistics", "/statistics")
      )
    )
  )
}