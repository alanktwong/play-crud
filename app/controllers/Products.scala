package controllers

import play.api.mvc._

import util.{Try, Success, Failure}
import models.{ProductData, Product}
import misc.{Jsons, Constants}
import play.api.data.Form
import play.api.data.Forms.{mapping, longNumber, nonEmptyText}
import play.api.i18n.Messages
import play.api.libs.json._


class Products extends BaseController {
	
	private def productForm(isEdit: Boolean): Form[ProductData] = {
		if (isEdit) {
			Form(mapping(Constants.EAN -> longNumber.verifying(
					"validation.ean.exists", Product.findByEan(_).isDefined),
				Constants.NAME -> nonEmptyText,
				Constants.DESCRIPTION -> nonEmptyText
			)(ProductData.apply)(ProductData.unapply))
		} else {
			Form(mapping(Constants.EAN -> longNumber.verifying(
					"validation.ean.duplicate", Product.findByEan(_).isEmpty),
				Constants.NAME -> nonEmptyText,
				Constants.DESCRIPTION -> nonEmptyText
			)(ProductData.apply)(ProductData.unapply))
		}
	}
	

	def list = ViewContextAction { implicit context =>
		render {
			case Accepts.Html() => {
				val products = Product.findAll
				Ok(views.html.products.productListing(products))
			}
			case Accepts.Json() => {
				val productCodes = Product.findAll.map(_.ean)
				Ok(Jsons.toJsValue(productCodes))
			}
		}
	}
	

	/**
	 * TODO AKW: generalize this
	 * 
	 * @see http://workwithplay.com/blog/2013/05/15/json-rest-web-services/
	 */
	def show(ean: Long) =  ViewContextAction { implicit context =>
		render {
			case Accepts.Html() => {
				Product.findByEan(ean).map { product =>
					Ok(views.html.products.product(product))
				}.getOrElse(NotFound)
			}
			case Accepts.Json() => {
				Product.findByEan(ean).map { product =>
					Ok(Jsons.fromProduct(product))
				}.getOrElse(NotFound)
			}
		}
	}


	def delete(ean: Long) = ViewContextAction { implicit context => 
		NotImplemented
	}

	def edit(ean: Long) = ViewContextAction { implicit context =>
		render {
			case Accepts.Html() => {
				Product.findByEan(ean) match {
					case Some(prod) =>
						logger.info("found product: ean = " + ean)
						val form = getProductForm(context, Some(prod))
						Ok(views.html.products.editProduct(form, true)).withSession(
							session + ("ean" -> ean.toString))
					case None => Redirect(routes.Products.list)
				}
			}
			case Accepts.Json() => {
				Redirect(routes.Products.show(ean))
			}
		}
	}

	
	def newProduct = ViewContextAction { implicit context =>
		render {
			case Accepts.Html() => {
				val form = getProductForm(context, None)
				Ok(views.html.products.editProduct(form, false))
			}
			case Accepts.Json() => {
				Redirect(routes.Products.list)
			}
		}
	}

	
	private def getProductForm(request: Request[AnyContent], prodOpt: Option[ProductData]): Form[ProductData] = {
		val flash = request.flash
		val aForm = prodOpt match {
			case Some(p) => productForm(true).fill(p)
			case None => productForm(false)
		}
		request.flash.get("error") match {
			case Some(_) => aForm.bind(flash.data)
			case None => aForm
		}
	}
	
	private def isEdit(request: Request[AnyContent]): Boolean = {
		request.session.get("ean") match {
			case Some(_) => true
			case _ => false
		}
	}
	private def getEanFromSession(request: Request[AnyContent]): Option[Long] = {
		request.session.get("ean") match {
			case Some(s) => {
				try {
					Some(s.toLong)
				} catch {
					case e:Throwable => None
				}
			}
			case _ => None
		}
	}

	def save = ViewContextAction { implicit context => 
		render {
			case Accepts.Html() => {
				saveForm(context)
			}
			case Accepts.Json() => {
				context.body.asJson.map { jsValue =>
					Jsons.toProduct(jsValue).fold(
						valid = { productToSave =>
							Product.save(productToSave) match {
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
	
	
	
	private def saveForm(implicit request: Request[AnyContent]): Result = {
		val isEditing = isEdit(request)
		val enteredProductForm = productForm(isEditing).bindFromRequest()

		enteredProductForm.fold(
			hasErrors = { form =>
				if (isEditing) {
					getEanFromSession(request) match {
						case Some(ean) =>
							Redirect(routes.Products.edit(ean)).
								flashing(Flash(form.data) +
								("error" -> Messages("validation.errors")))
						case _ => 
							Redirect(routes.Products.list())
					}
				} else {
					Redirect(routes.Products.newProduct).
						flashing(Flash(form.data) +
						("error" -> Messages("validation.errors")))
					
				}
			
			},
			success = { productToSave =>
				Product.save(productToSave)
				if (isEditing) {
					val message = Messages("products.new.success", productToSave.name)
					Redirect(routes.Products.show(productToSave.ean)).
						flashing("success" -> message).withSession(session - "ean")
					
				} else {
					val message = Messages("products.edit.success", productToSave.name)
					Redirect(routes.Products.show(productToSave.ean)).
						flashing("success" -> message)
				}
			}
		)
	}
}

