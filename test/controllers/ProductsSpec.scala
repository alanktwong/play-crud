package controllers

import models._
import misc.{Jsons, Constants}

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers

import org.squeryl.PrimitiveTypeMode.inTransaction

import play.api.http.ContentTypes.JSON
import play.api.test._
import play.api.test.Helpers._

import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith

@RunWith(classOf[JUnitRunner])
class ProductsSpec extends FlatSpec with ShouldMatchers {

	"A form post to the save action" should "respond" in {
		running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
			val dataMap = Map(Constants.EAN -> "1234", Constants.NAME -> "name", Constants.DESCRIPTION -> "description")
			
			val products = new controllers.Products
			val result = products.save(FakeRequest().withFormUrlEncodedBody(dataMap.toSeq:_*))
			status(result) should equal (SEE_OTHER)
			redirectLocation(result) should equal (Some(routes.Products.list))
		}
	}
	
	"A post to the Products" should "update" in {
		running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
			import play.api.libs.json.Json
			val jsonString  = """{ "ean" : 5010255079763, "name" : "Paperclips Lg", "description" : "Large Plain Pack of 1000" }"""
			val headerMap = Map("Accept" -> "application/json")
			val products = new controllers.Products
			val result = products.save(FakeRequest().withJsonBody(Json.parse(jsonString)).withHeaders(headerMap.toSeq:_*))
			
			status(result) should not equal (NOT_ACCEPTABLE)
			status(result) should equal (OK)
		}
	}
}