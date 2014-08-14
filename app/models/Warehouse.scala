package models

import org.squeryl.KeyedEntity
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.dsl.{OneToMany, ManyToOne}
import org.squeryl.{Query, Schema, KeyedEntity, Table}

import collection.Iterable
import util.{Try, Success, Failure}
import Database._

case class WarehouseData(code: String, name: String) {
	private def maybeWarehouse: Option[Warehouse] = {
		Warehouse.findWarehouseByCode(code)
	}
	def id: Long = {
		maybeWarehouse match {
			case Some(wh) => wh.id
			case None => 0L
		}
	}
	
	def stockItems: Seq[StockItemData] = {
		maybeWarehouse match {
			case Some(wh) =>
				Warehouse.getStockItems(wh) map { _.toData.get }
			case None =>
				Seq[StockItemData]()
		}
	}
}

private[models] class Warehouse(val id: Long, var code: String, var name: String) extends KeyedEntity[Long] {
	def this() = this(0L,"","")
	
	def toData: WarehouseData = WarehouseData(code, name)
	
	lazy val stockItems: OneToMany[StockItem] = Database.warehouse2StockItems.left(this)
}

object Warehouse extends misc.Logging {
	private[models] def getStockItems(warehouse: Warehouse): Seq[StockItem] = inTransaction {
		warehouse.stockItems.toIndexedSeq
	}

	def findAll: Seq[WarehouseData] = inTransaction {
		val query = from(warehousesTable) { warehouse =>
			select(warehouse).
			orderBy(warehouse.name desc)
		}
		query.toList map { _.toData }
	}

	private[models] def findWarehouseByCode(code: String): Option[Warehouse] = inTransaction {
		from(warehousesTable){ warehouse =>
			where(warehouse.code === code).
			select(warehouse)
		}.headOption
	}
	
	def findByCode(code: String): Option[WarehouseData] = inTransaction {
		for (w <- findWarehouseByCode(code)) yield w.toData
	}

	def save(data: WarehouseData): Try[WarehouseData] = inTransaction {
		findWarehouseByCode(data.code) match {
			case Some(w) =>
				update(w)(data)
			case None =>
				insert(data)
		}
	}
	private[models] def update(w: Warehouse)(data: WarehouseData): Try[WarehouseData] = inTransaction {
		try {
			val warehouseToBeUpdated = new Warehouse(w.id, w.code, data.name)
			warehousesTable.update(warehouseToBeUpdated)
			logger.debug(String.format("Updated  warehouse code: %s with id: %s", w.code.toString, w.id.toString))
			Success(warehouseToBeUpdated.toData)
		} catch {
			case t: Throwable =>
				logger.error("failed on warehouse update")
				Failure(t)
		}
	}
	
	private[models] def insert(data: WarehouseData): Try[WarehouseData] = inTransaction {
		try {
			val warehouseToBeInserted = new Warehouse(0L, data.code, data.name)
			val w = warehousesTable.insert(warehouseToBeInserted)
			logger.debug(String.format("Inserted warehouse code: %s with id: %s", w.code.toString, w.id.toString))
			require (w.id > 0)
			Success(w.toData)
		} catch {
			case t: Throwable =>
				logger.error("failed on warehouse insert")
				Failure(t)
		}
	}
	
	def delete(data: WarehouseData): Try[WarehouseData] = inTransaction {
		try {
			val int = warehousesTable.deleteWhere( wh => wh.code === data.code )
			logger.info(String.format("delete of warehouse: ean %s returned %s", data.code.toString, int.toString))
			Success(data)
//			val maybeWarehouse = findWarehouseByCode(data.code)
//			val maybeStockItem = StockItem.findByWarehouse(data)
//			if (maybeWarehouse.isDefined && maybeStockItem.isEmpty) {
//			} else {
//				toFailure("cannot delete warehouse")
//			}
		} catch {
			case t: Throwable =>
				logger.error("failed on warehouse delete")
				Failure(t)
		}
	}
	
}