package models

import models._
import misc.{AbstractFlatSpec, Constants}

import org.scalatest.FlatSpec
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import util.{Try, Success, Failure}

import org.squeryl.PrimitiveTypeMode.inTransaction

import play.api.test._
import play.api.test.Helpers._

import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith


@RunWith(classOf[JUnitRunner])
class WarehouseSpec extends AbstractFlatSpec {

	"A warehouse" should "be creatable" in {
		databaseTesting {
			val warehouseToBeInserted = WarehouseData("Wxf", "description")
			val tryWarehouse = Warehouse.save(warehouseToBeInserted)
			tryWarehouse match {
				case Success(wh) =>
					wh.id should not equal (0)
				case Failure(t) =>
					fail
			}
		}
	}

	"A warehouse" should "be updateable" in {
		databaseTesting {
			val warehouseToBeUpdated = Constants.initialWarehousesMap.head._2 copy (name = "alibaba")
			
			val maybeWarehouse = Warehouse.save(warehouseToBeUpdated)
			maybeWarehouse match {
				case Success(wh) =>
					wh.name should equal(warehouseToBeUpdated.name)
				case Failure(_) =>
					fail
			}
		}
	}
	
	"A warehouse without stock items" should "be deleteable" in {
		databaseTesting {
			val warehouseToBeDeleted = Constants.initialWarehousesMap.head._2
			
			val tryWarehouse = Warehouse.delete(warehouseToBeDeleted)
			tryWarehouse match {
				case Success(warehouse) =>
					warehouse.code should equal(warehouseToBeDeleted.code)
				case Failure(_) =>
					fail
			}
		}
	}
	"A warehouse with stock items" should "not be deleteable" in {
		databaseTesting {
			StockItem.save(Constants.initialStockItem.get) match {
				case Failure(_) =>
					fail
				case Success(savedStockItem) =>
					val tryToDeleteStockItem = StockItem.delete(savedStockItem)
					tryToDeleteStockItem match {
						case Success(_) =>
							fail
						case Failure(th) =>
							savedStockItem.id should not equal (0)
					}
			}
		}
	}

}
