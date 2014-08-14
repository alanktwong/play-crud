package models

import models._
import misc.{Constants, AbstractFlatSpec}


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
class StockItemSpec extends AbstractFlatSpec {

	"A stock item" should "be creatable" in {
		databaseTesting {
			val quantity = 100L
			
			val product = ProductData(512431289L, "santa's sleigh", "a very long sleigh")
			val warehouse = WarehouseData("W1243128947", "north pole")
			val stockItem = StockItemData(product.ean, warehouse.code, quantity)
			
			val tryProduct = Product.insert(product)
			val tryWarehouse = Warehouse.insert(warehouse)
			
			val tryBoth = for (p <- tryProduct; w <- tryWarehouse) yield (p,w)
			tryBoth match {
				case Success( (product,warehouse) ) => {
					val stockItemTry = StockItem.save(stockItem)
					stockItemTry match {
						case Failure(_) =>
							logger.error("could not create a stock item")
							fail
						case Success(si) =>
							si.id should not equal(0)
					}
				}
				case Failure(_) =>
					logger.error("failed on tryProduct or tryWarehouse")
					fail
			}
		}
	}
	
	"A stock item" should "be updateable" in {
		databaseTesting {
			val quantity = 1000L
			val initialData = Constants.initialStockItem.get
			
			val data = StockItemData(initialData.ean, initialData.warehouseCode, quantity)
			StockItem.findByStockItemData(data) match {
				case None =>
					logger.error("this stock item does not exist")
					fail
				case Some(extantStockItem) =>
					StockItem.save(data) match {
						case Success(savedStockItem) =>
							extantStockItem.quantity should not equal(quantity)
							savedStockItem.quantity should equal(quantity)
						case Failure(_) =>
							fail
					}
			}
		}
	}
	
	"A stock item" should "be deletable" in {
		databaseTesting {
			val stockItemToBeDeleted = Constants.initialStockItem.get
			
			StockItem.delete(stockItemToBeDeleted) match {
				case Failure(_) =>
					logger.error("this stock item does not exist")
					fail
				case Success(extantStockItem) =>
					StockItem.delete(stockItemToBeDeleted) match {
						case Success(deletedStockItem) =>
							deletedStockItem.quantity should equal(stockItemToBeDeleted.quantity)
						case Failure(_) =>
							fail
					}
			}
		}
	}
}