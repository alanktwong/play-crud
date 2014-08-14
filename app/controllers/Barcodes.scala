package controllers

import play.api.mvc.{Request, Result, Flash, Action}
import util.{Try, Success, Failure}
import play.api.libs.concurrent.Execution.Implicits._

import services.BarcodeService
import misc.Constants


class Barcodes(barcodeService: BarcodeService) extends BaseController {

	def barcode(ean: Long) =  Action { implicit request => 
		Async {
			barcodeService.renderImage(ean) map {
				case Success(image) => Ok(image).as(Constants.MIME_TYPE)
				case Failure(e) =>
					BadRequest("Couldn’t generate bar code. Error: " + e.getMessage)
			}
		}
	}

}
