package controllers

import play.api.mvc.{Controller, Action}

object IndexPage extends Controller with ApplicationMenu {
  def index = Action {
    Ok(views.html.index(Settings.retrieve().isEmpty)(sections))
  }
}
