package controllers

import util.{Try, Success, Failure}

import play.api.mvc.{Request, Result, Flash, Action}
import play.api.libs.json._
import play.api.data.Form
import play.api.data.Forms.{mapping, longNumber, nonEmptyText}

import models._
import misc.{Jsons, Constants}

class StockItems extends BaseController {
  
	private def warehouseForm(isEdit: Boolean): Form[StockItemData] = {
		if (isEdit) {
			Form(mapping(Constants.STOCK_ITEM_EAN -> longNumber.verifying(
					"validation.ean.exists", Product.findByEan(_).isDefined),
				Constants.STOCK_ITEM_CODE -> nonEmptyText.verifying(
					"validation.code.exists", Warehouse.findByCode(_).isDefined),
				Constants.STOCK_ITEM_QUANTITY -> longNumber
			)(StockItemData.apply)(StockItemData.unapply))
		} else {
			Form(mapping(Constants.STOCK_ITEM_EAN -> longNumber.verifying(
					"validation.ean.exists", Product.findByEan(_).isDefined),
				Constants.STOCK_ITEM_CODE -> nonEmptyText.verifying(
					"validation.code.exists", Warehouse.findByCode(_).isDefined),
				Constants.STOCK_ITEM_QUANTITY -> longNumber
			)(StockItemData.apply)(StockItemData.unapply))
		}
	}
	

	def list = Action { implicit request =>
		render {
			case Accepts.Html() => {
				val stockItems = StockItem.findAll
				Ok(views.html.stockitems.stockItemsListing(stockItems))
			}
			case Accepts.Json() => {
				val stockItems = StockItem.findAll
				Ok(Jsons.fromStockItems(stockItems))
			}
		}
	}
	
	def show(ean: Long, code: String) =  Action { implicit request =>
		render {
			case Accepts.Html() => {
				StockItem.findByEanAndWarehouseCode(ean, code).map { stockItem =>
					Ok(views.html.stockitems.stockItem(stockItem))
				}.getOrElse(NotFound)
			}
			case Accepts.Json() => {
				StockItem.findByEanAndWarehouseCode(ean, code).map { stockItem =>
					Ok(Jsons.fromStockItem(stockItem))
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
	
	def edit(ean: Long, code: String) =  Action { implicit request =>
		render {
			case Accepts.Html() => {
				NotImplemented
			}
			case Accepts.Json() => {
				Redirect(routes.StockItems.show(ean, code))
			}
		}
	}
	
	def delete(ean: Long, code: String) =  Action { implicit request =>
		render {
			case Accepts.Html() => {
				NotImplemented
			}
			case Accepts.Json() => {
				NotImplemented
			}
		}
	}
	
	def newStockItem =  Action { implicit request =>
		render {
			case Accepts.Html() => {
				NotImplemented
			}
			case Accepts.Json() => {
				Redirect(routes.Products.list)
			}
		}
	}

}