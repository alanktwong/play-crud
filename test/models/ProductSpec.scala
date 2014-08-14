package models

import models._
import misc.{Jsons, Constants}

import util.{Try, Success, Failure}
import org.squeryl.PrimitiveTypeMode.inTransaction
import play.api.test._
import play.api.test.Helpers._
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith

import misc.AbstractFlatSpec

@RunWith(classOf[JUnitRunner])
class ProductSpec extends AbstractFlatSpec {

	"A product" should "be creatable" in {
		databaseTesting {
			val prodToBeInserted = ProductData(123L, "name", "description")
			val maybeProd = Product.save(prodToBeInserted)
			maybeProd match {
				case Success(prod) =>
					prod.id should not equal(0)
				case Failure(t) =>
					fail
			}
		}
	}
	
	"A product" should "be updateable" in {
		databaseTesting {
			val prodToBeUpdated = Constants.initialProductsMap.head._2 copy (description = "alab")
			
			val maybeProd = Product.save(prodToBeUpdated)
			maybeProd match {
				case Success(prod) =>
					prod.description should equal(prodToBeUpdated.description)
				case Failure(_) =>
					fail
			}
		}
	}
	
	"A product" should "be deleteable" in {
		databaseTesting {
			val prodToBeDeleted = Constants.initialProductsMap.head._2
			
			val tryProd = Product.delete(prodToBeDeleted)
			tryProd match {
				case Success(prod) =>
					prod.ean should equal(prodToBeDeleted.ean)
				case Failure(_) =>
					fail
			}
		}
	}
	
	"A product" should "be serializable to JSON" in {
		databaseTesting {
			import play.api.libs.json._
			val map = Constants.initialProductsMap
			val product = map.head._2
			
			val productJson = Jsons.fromProduct(product)
			(productJson \ "ean").asOpt[Long] match {
				case Some(eanActual) =>
					eanActual should equal(product.ean)
				case None =>
					fail
			}
		}
	}
}
