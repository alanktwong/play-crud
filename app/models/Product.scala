package models

import org.squeryl.PrimitiveTypeMode._
import org.squeryl.dsl.{OneToMany, ManyToOne}
import org.squeryl.{Query, Schema, KeyedEntity, Table}

import collection.Iterable
import util.{Try, Success, Failure}
import Database._

case class ProductData(ean: Long, name: String, description: String) {
	private def maybeProduct = {
		Product.findProductByEan(ean)
	}

	def id: Long = {
		maybeProduct match {
			case Some(product) => product.id
			case None => 0L
		}
	}
	def stockItems: Seq[StockItemData] = {
		maybeProduct match {
			case Some(p) =>
				Product.getStockItems(p) map { _.toData.get }
			case None =>
				Seq[StockItemData]()
		}
	}
}

private[models] class Product(val id: Long, var ean: Long, var name: String, var description: String) extends KeyedEntity[Long] {
	def this() = this(0L,0L,"","")
	
	def toData: ProductData = ProductData(ean, name, description)
	
	lazy val stockItems: OneToMany[StockItem] = Database.product2StockItems.left(this)
}

object Product extends misc.Logging {
  
	private[models] def getStockItems(product: Product): Seq[StockItem] = inTransaction {
		product.stockItems.toIndexedSeq
	}

	def findAll: Seq[ProductData] = inTransaction {
		val query = from(productsTable) {
			product => select(product)
		}
		val products = query.toList.sortBy( _.ean )
		products map { _.toData }
	}

	private[models] def findProductByEan(ean: Long): Option[Product] = inTransaction {
		from(productsTable){ product =>
			where(product.ean === ean)
			select(product)
		}.headOption
	}
	
	def findByEan(ean: Long): Option[ProductData] = inTransaction {
		val maybeProduct = findProductByEan(ean)
		for (p <- maybeProduct) yield p.toData
	}
	
	private def productsInWarehouse(warehouseData: WarehouseData): Query[Product] = {
		from(productsTable, stockItemsTable, warehousesTable){ (product, stockItem, warehouse) =>
			where(warehouse.code === warehouseData.code 
				and stockItem.productId === product.id
				and stockItem.warehouseId === warehouse.id)
			select(product)
		}
	}
	
	
	def findProductsInWarehouse(warehouse: WarehouseData): Seq[ProductData] = inTransaction {
		productsInWarehouse(warehouse).toSeq map { _.toData }
	}
	
	def findProductsInWarehouseAndName(name: String, warehouse: WarehouseData): Seq[ProductData] = inTransaction {
		def productsInWarehouseByName(name: String, warehouse: WarehouseData): Query[Product] = {
			from(productsInWarehouse(warehouse) ){ product =>
				where(product.name like name).
				select(product)
			}
		}
		productsInWarehouseByName(name, warehouse).toSeq map { _.toData }
	}

	def save(data: ProductData): Try[ProductData] = inTransaction {
		findProductByEan(data.ean) match {
			case Some(p) => 
				update(p)(data)
			case None =>
				insert(data)
		}
	}
	
	private[models] def update(p: Product)(data: ProductData): Try[ProductData] = inTransaction {
		try {
			val productToUpdate = new Product(p.id, p.ean, data.name, data.description)
			productsTable.update(productToUpdate)
			logger.debug(String.format("Updated  product ean: %s with id: %s", p.ean.toString, p.id.toString))
			Success(data)
		} catch {
			case t: Throwable =>
				logger.error("failed on product update")
				Failure(t)
		}
	}
	
	private[models] def insert(data: ProductData): Try[ProductData] = inTransaction {
		try {
			val productToInsert = new Product(0L, data.ean, data.name, data.description)
			val p = productsTable.insert(productToInsert)
			logger.debug(String.format("Inserted product ean: %s with id: %s", p.ean.toString, p.id.toString))
			require (p.id > 0)
			Success(p.toData)
		} catch {
			case t: Throwable =>
				logger.error("failed on product insert")
				Failure(t)
		}
	}
	
	def delete(data: ProductData): Try[ProductData] = inTransaction {
		try {
			val int = productsTable.deleteWhere( product => product.ean === data.ean )
			logger.info(String.format("delete of product: ean %s returned %s", data.ean.toString, int.toString))
			Success(data)
		} catch {
			case t: Throwable =>
				logger.error("failed on product delete")
				Failure(t)
		}
	}
}





