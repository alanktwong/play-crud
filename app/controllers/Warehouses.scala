package controllers

import util.{Try, Success, Failure}

import play.api.mvc.{Request, Result, Flash, Action}
import play.api.libs.json._
import play.api.data.Form
import play.api.data.Forms.{mapping, longNumber, nonEmptyText}
import models._
import misc.{Jsons, Constants}

class Warehouses extends BaseController {

	private def warehouseForm(isEdit: Boolean): Form[WarehouseData] = {
		if (isEdit) {
			Form(mapping(Constants.WH_CODE -> nonEmptyText.verifying(
					"validation.code.exists", Warehouse.findByCode(_).isDefined),
				Constants.WH_NAME -> nonEmptyText
			)(WarehouseData.apply)(WarehouseData.unapply))
		} else {
			Form(mapping(Constants.WH_CODE -> nonEmptyText.verifying(
					"validation.ean.duplicate", Warehouse.findByCode(_).isEmpty),
				Constants.WH_NAME -> nonEmptyText
			)(WarehouseData.apply)(WarehouseData.unapply))
		}
	}
	
	def list = Action { implicit request =>
		render {
			case Accepts.Html() => {
				val warehouses = Warehouse.findAll
				Ok(views.html.warehouses.warehouseListing(warehouses))
			}
			case Accepts.Json() => {
				val warehouses = Warehouse.findAll
				Ok(Jsons.fromWarehouses(warehouses))
			}
		}
	}
	
	def show(code: String) =  Action { implicit request =>
		render {
			case Accepts.Html() => {
				Warehouse.findByCode(code).map { warehouse =>
					Ok(views.html.warehouses.warehouse(warehouse))
				}.getOrElse(NotFound)
			}
			case Accepts.Json() => {
				Warehouse.findByCode(code).map { warehouse =>
					Ok(Jsons.fromWarehouse(warehouse))
				}.getOrElse(NotFound)
			}
		}
	}
	
	def save =  Action { implicit request =>
		render {
			case Accepts.Html() => {
				NotImplemented
			}
			case Accepts.Json() => {
				request.body.asJson.map { jsValue =>
					Jsons.toWarehouse(jsValue).fold(
						valid = { warehouseToSave =>
							Warehouse.save(warehouseToSave) match {
								case Success(_) =>
									Created("saved")
								case Failure(_) =>
									NotAcceptable("failed")
							}
						},
						invalid = {
							errors => BadRequest(JsError.toFlatJson(errors))
						}
					)
				}.getOrElse {
					BadRequest("malformed json")
				}
			}
		}
	}
	
	def edit(code: String) =  Action { implicit request =>
		render {
			case Accepts.Html() => {
				NotImplemented
			}
			case Accepts.Json() => {
				Redirect(routes.Warehouses.show(code))
			}
		}
	}
	
	def delete(code: String) =  Action { implicit request =>
		render {
			case Accepts.Html() => {
				NotImplemented
			}
			case Accepts.Json() => {
				NotImplemented
			}
		}
	}
	
	def newWarehouse =  Action { implicit request =>
		render {
			case Accepts.Html() => {
				NotImplemented
			}
			case Accepts.Json() => {
				Redirect(routes.Warehouses.list)
			}
		}
	}

}