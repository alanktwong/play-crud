package controllers

import play.api.mvc.{Request, Result, Flash, Action}


class Application extends BaseController {

	def index = ViewContextAction { implicit context =>
		Redirect(routes.Products.list())
	}

	def dashboard =  ViewContextAction { implicit context =>
		Ok(views.html.dashboard.dashboardHome())
	}
}
