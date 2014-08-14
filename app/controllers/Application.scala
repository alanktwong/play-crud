package controllers

import play.api.mvc.{Request, Result, Flash, Action}


class Application extends BaseController {

	def index = Action { implicit request =>
		Redirect(routes.Products.list())
	}

	def dashboard =  Action { implicit request =>
		Ok(views.html.dashboard.dashboardHome())
	}
}
