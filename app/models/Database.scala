package models

import org.squeryl.PrimitiveTypeMode._
import org.squeryl.ForeignKeyDeclaration
import org.squeryl.dsl.{OneToMany, ManyToOne}
import org.squeryl.{Query, Schema, KeyedEntity, Table}

import misc.Constants

object Database extends Schema with misc.Logging {
	val productsTable = table[Product]("products")
	val stockItemsTable = table[StockItem]("stock_items")
	val warehousesTable = table[Warehouse]("warehouses")
	
	on(productsTable) { table => declare {
			table.id is (autoIncremented)
			table.ean is (unique)
		}
	}
	on(stockItemsTable) { table => declare {
			table.id is (autoIncremented)
			columns(table.productId, table.warehouseId) are (unique)
		}
	}
	on(warehousesTable) { table => declare {
			table.id is (autoIncremented)
			table.code is (unique)
		}
	}
	
	val product2StockItems = oneToManyRelation(productsTable, stockItemsTable).via {
		(p,s) => p.id === s.productId
	}
	val warehouse2StockItems = oneToManyRelation(warehousesTable, stockItemsTable).via {
		(w,s) => w.id === s.warehouseId
	}
	
	override def applyDefaultForeignKeyPolicy(foreignKeyDeclaration: ForeignKeyDeclaration) =
			foreignKeyDeclaration.constrainReference

	
	def initializeDomain() = {
		val products = Product.findAll
		if (products.isEmpty) {
			val initialProducts = Constants.initialProducts
			logger.info(String.format("Populating products: %s", initialProducts map (_.ean)))
			val productTries = initialProducts map {
				Product.insert(_)
			}
			val prodResult = Product.findAll
			logger.info(String.format("Populated %s products", prodResult.size.toString))
		}
		val warehouses = Warehouse.findAll
		if (warehouses.isEmpty) {
			val initialWarehouses = Constants.initialWarehouses
			logger.info(String.format("Populating warehouses: %s", initialWarehouses map(_.code)))
			val warehouseTries = initialWarehouses.foreach {
				Warehouse.insert(_)
			}
			val warehouseResult = Warehouse.findAll
			logger.info(String.format("Populated %s warehouses", warehouseResult.size.toString))
		}
		val stockItems = StockItem.findAll
		if (stockItems.isEmpty) {
			logger.info("Populating stock items")
			Constants.initialStockItem.foreach { si =>
				StockItem.save(si)
			}
		}
	}	
}
	