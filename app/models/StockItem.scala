package models

import org.squeryl.KeyedEntity
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.dsl.{OneToMany, ManyToOne}
import org.squeryl.{Query, Schema, KeyedEntity, Table}

import collection.Iterable
import util.{Try, Success, Failure}
import Database._


case class StockItemData(ean: Long, warehouseCode: String, quantity: Long) {
	private def maybeStockItem = {
		StockItem.findStockItemByData(this)
	}
	def id: Long = {
		maybeStockItem match {
			case Some(si) => si.id
			case None => 0L
		}
	}
	
	def warehouse: Option[WarehouseData] = {
		maybeStockItem match {
			case Some(si) => 
				val warehouseData = StockItem.getWarehouse(si).toData
				Some(warehouseData)
			case None => None
		}
	}
	def product: Option[ProductData] = {
		maybeStockItem match {
			case Some(si) => 
				val productData = StockItem.getProduct(si).toData
				Some(productData)
			case None => None
		}
	}
}

object StockItemData {
	
	def create(product: ProductData, warehouse: WarehouseData, quantity: Long): StockItemData = {
		StockItemData(product.ean, warehouse.code, quantity)
	}

}


private[models] class StockItem(val id: Long, val productId: Long, val warehouseId: Long, var quantity: Long) extends KeyedEntity[Long] {
	def this() = this(0L,0L,0L,0L)
	
	def toData: Option[StockItemData] = {
		val maybeProduct = product.headOption
		val maybeWarehouse = warehouse.headOption
	
		val result = for (product <- maybeProduct; warehouse <- maybeWarehouse)
			yield StockItemData(product.ean, warehouse.code, quantity)
		result
	}
	
	lazy val product: ManyToOne[Product] = Database.product2StockItems.right(this)
	lazy val warehouse: ManyToOne[Warehouse] = Database.warehouse2StockItems.right(this)
}


object StockItem extends misc.Logging {
	private[models] def getProduct(stockItem: StockItem): Product = inTransaction {
		stockItem.product.head
	}
	private[models] def getWarehouse(stockItem: StockItem): Warehouse = inTransaction {
		stockItem.warehouse.head
	}

	
	private[models] def toData(maybeStockItem: Option[StockItem]): Option[StockItemData] = {
		maybeStockItem match {
			case Some(stockItem) => stockItem.toData
			case None => None
		}
	}
	
	private[models] def findStockItemByData(stockItemData: StockItemData): Option[StockItem]= inTransaction {
		findStockItemByEanAndWarehouseCode(stockItemData.ean, stockItemData.warehouseCode)
	}
	private[models] def findStockItemByEanAndWarehouseCode(ean: Long, warehouseCode: String): Option[StockItem]= inTransaction {
		val query = from(productsTable,stockItemsTable, warehousesTable){ (p,si,w) =>
			where(si.productId === p.id and si.warehouseId === w.id and p.ean === ean and w.code === warehouseCode).
			select(si)
		}
		query.headOption
	}
	
	def findByEanAndWarehouseCode(ean: Long, warehouseCode: String): Option[StockItemData]= inTransaction {
		val query = from(productsTable,stockItemsTable, warehousesTable){ (p,si,w) =>
			where(si.productId === p.id and si.warehouseId === w.id and p.ean === ean and w.code === warehouseCode).
			select(p.ean, w.code, si.quantity)
		}
		query.headOption match {
			case Some( (ean, code, quantity) ) => Some(StockItemData(ean, code, quantity))
			case None => None
		}
	}
	
	def findByStockItemData(stockItemData: StockItemData): Option[StockItemData] = inTransaction {
		findByEanAndWarehouseCode(stockItemData.ean, stockItemData.warehouseCode)
	}
	
	def findByProductAndWarehouse(product: ProductData, warehouse: WarehouseData): Option[StockItemData] = inTransaction {
		findByEanAndWarehouseCode(product.ean, warehouse.code)
	}
	
	private def findById(id: Long): Option[StockItemData] = inTransaction {
		val query = from(stockItemsTable){ stockItem =>
			where(stockItem.id === id).
			select(stockItem)
		}
		toData(query.headOption)
	}
	
	
	def findAll: Seq[StockItemData] = inTransaction {
		val query = from(productsTable,stockItemsTable, warehousesTable) {
			(p, si, w) => where(si.productId === p.id and si.warehouseId === w.id).
			select(si).
			orderBy(si.productId asc)
		}
		val resultSet = query.toList.map{ _.toData }.toSet
		val data = for {m <- resultSet if m.isDefined} yield m.get
		logger.debug("found {} stock items", data.size)
		data.toSeq
	}
	
	def save(data: StockItemData): Try[StockItemData] = inTransaction {
		findStockItemByData(data) match {
			case Some(stockItem) =>
				update(stockItem)(data)
			case None => {
				insert(data)
			}
		}
	}
	
	private[models] def update(stockItem: StockItem)(data: StockItemData): Try[StockItemData] = inTransaction {
		try {
			val stockItemToUpdate = new StockItem(stockItem.id, stockItem.productId, stockItem.warehouseId, data.quantity)
			stockItemsTable.update(stockItemToUpdate)
			logger.debug(String.format("Updated stockItem with id: %s", stockItemToUpdate.id.toString))
			Success(data)
		} catch {
			case t:Throwable =>
				logger.error("failed on stock item update")
				Failure(t)
		}
	}
	
	private[models] def insert(data: StockItemData): Try[StockItemData] = inTransaction {
		val maybeProduct = Product.findProductByEan(data.ean)
		maybeProduct match {
			case Some(product) => {
				val maybeWarehouse = Warehouse.findWarehouseByCode(data.warehouseCode)
				maybeWarehouse match {
					case None => 
						val msg  = "cannot create a stock item at a non existent warehouse"
						logger.error(msg)
						toFailure(msg)
					case Some(warehouse) =>
						val stockItemToInsert = new StockItem(0L, product.id, warehouse.id, data.quantity)
						logger.trace(String.format("Inserting stockItem with {id: %s, productId: %s, warehouseId: %s, quantity: %s}",
								stockItemToInsert.id.toString, stockItemToInsert.productId.toString,
								stockItemToInsert.warehouseId.toString, stockItemToInsert.quantity.toString))
						val si = stockItemsTable.insert(stockItemToInsert)
						logger.debug(String.format("Inserted stockItem with id: %s", si.id.toString))
						require(stockItemToInsert.id > 0)
						Success(data)
				}
			}
			case None =>
				val msg  = "cannot create a stock item for a non existent product"
				logger.error(msg)
				toFailure(msg)
		}
	}
	def findByWarehouse(data: WarehouseData): Seq[StockItemData] = inTransaction {
		throw new IllegalArgumentException
	}
	
	def delete(data: StockItemData): Try[StockItemData] = inTransaction {
		findStockItemByData(data) match {
			case Some(stockItemToBeDeleted) =>
				try {
					val int = stockItemsTable.deleteWhere( si => si.id === stockItemToBeDeleted.id )
					logger.info(String.format("delete of stock item: %s returned %s", stockItemToBeDeleted.id.toString, int.toString))
					Success(data)
				} catch {
					case th: Throwable =>
						logger.error("failed on stock item delete")
						Failure(th)
				}
			case None =>
				val msg = "cannot delete a non existent stock item" 
				logger.error(msg)
				toFailure(msg)
		}
	}
	
}
