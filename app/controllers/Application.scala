package controllers

import play.api._
import play.api.mvc._

case class Section(name: String, items: Seq[MenuItem])
case class MenuItem(name: String, url: String)

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