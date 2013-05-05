package controllers

import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import anorm._
import anorm.SqlParser._
import play.api.db.DB
import play.api.Play.current

case class Settings(host: String, port: Int)

object Settings {
  val parser = get[String]("host") ~ get[Int]("port") map {
    case h~p => new Settings(h, p)
  }

  val default: Settings = new Settings("localhost", 7777)

  def retrieve(): Option[Settings] = {
    DB.withConnection { implicit c =>
      SQL("select top 1 host, port from settings").as(parser *).headOption
    }
  }

  def retrieveForHost(host: String): Option[Settings] = {
    DB.withConnection { implicit c =>
      SQL("select top 1 host, port from settings").on("host" -> host).as(parser *).headOption
    }
  }

  def deleteAll() {
    DB.withConnection { implicit connection =>
      SQL("delete from settings").execute()
    }
  }

  def insert(settings: Settings): Option[Settings] = {
    DB.withConnection{ implicit connection =>
      SQL("insert into settings (host, port) values ({host}, {port})").on("host" -> settings.host, "port" -> settings.port).execute()
    }
    retrieve()
  }

  def update(settings: Settings): Option[Settings] = {
    DB.withConnection{ implicit connection =>
      SQL("update settings set host = {host}, port = {port} where host = {host}").on("host" -> settings.host, "port" -> settings.port).execute()
    }
    retrieve()
  }

  def save(settings: Settings): Option[Settings] = {
    deleteAll()
    insert(settings)
  }
}

object SettingsPage extends Controller with ApplicationMenu {

  val settingsForm = Form(
    mapping(
      "host" -> text,
      "port" -> number(min=0, max=9999)
    ) (Settings.apply)(Settings.unapply)
  )

  def settings = Action { implicit request =>

    var settings = Settings.retrieve().getOrElse(Settings.default)

    settingsForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.settings(sections)(formWithErrors)),
      value => {
        settings = Settings.save(value).getOrElse(settings)
      }
    )

    Ok(views.html.settings(sections)(settingsForm.fill(settings)))
  }
}