package misc

import models._
import play.api.libs.json._
import play.api.libs.functional.syntax._

object Jsons {
	
	def toJsValue[T](t: T)(implicit writes: Writes[T]): JsValue = {
		import play.api.libs.json._
		Json.toJson(t)(writes)
	}
	
	val productWrites: Writes[ProductData] = (
		(__ \ "ean" ).write[Long] and
		(__ \ "name" ).write[String] and
		(__ \ "description" ).write[String]
	)(unlift(ProductData.unapply))
	
	val productReads: Reads[ProductData] = (
		(__ \ "ean" ).read[Long] and
		(__ \ "name" ).read[String] and
		(__ \ "description" ).read[String]
	)(ProductData.apply _)
	
	val productFormat: Format[ProductData] = Format(productReads, productWrites)
	
	def fromProduct(product: ProductData): JsValue = {
		Json.toJson(product)(productFormat)
	}
	def toProduct(jsValue: JsValue): JsResult[ProductData] = {
		jsValue.validate[ProductData](productFormat)
	}
	
	private def fromIterable[T](xs: Iterable[T])(f: T => JsValue) = {
		Json.toJson(for (x <- xs) yield f(x))
	}
	
	def fromProducts(products: Iterable[ProductData]): JsValue = {
		fromIterable(products)(fromProduct)
	}
	
	val stockItemReads: Reads[StockItemData] = (
		(__ \ "ean" ).read[Long] and
		(__ \ "warehouseCode" ).read[String] and
		(__ \ "quantity" ).read[Long]
	)(StockItemData.apply _)
	
	val stockItemWrites: Writes[StockItemData] = (
		(__ \ "ean" ).write[Long] and
		(__ \ "warehouseCode" ).write[String] and
		(__ \ "quantity" ).write[Long]
	)(unlift(StockItemData.unapply))
	
	val stockItemFormat: Format[StockItemData] = Format(stockItemReads, stockItemWrites)
	
	def fromStockItem(obj: StockItemData): JsValue = {
		Json.toJson(obj)(stockItemFormat)
	}
	def toStockItem(jsValue: JsValue): JsResult[StockItemData] = {
		jsValue.validate[StockItemData](stockItemFormat)
	}
	def fromStockItems(items: Iterable[StockItemData]): JsValue = {
		fromIterable(items)(fromStockItem)
	}
	
	val warehouseReads: Reads[WarehouseData] = (
		(__ \ "code" ).read[String] and
		(__ \ "name" ).read[String]
	)(WarehouseData.apply _)
	
	val warehouseWrites: Writes[WarehouseData] = (
		(__ \ "code" ).write[String] and
		(__ \ "name" ).write[String]
	)(unlift(WarehouseData.unapply))
	
	val warehouseFormat: Format[WarehouseData] = Format(warehouseReads, warehouseWrites)
	
	def fromWarehouse(warehouse: WarehouseData): JsValue = {
		Json.toJson(warehouse)(warehouseFormat)
	}
	def toWarehouse(jsValue: JsValue): JsResult[WarehouseData] = {
		jsValue.validate[WarehouseData](warehouseFormat)
	}
	
	def fromWarehouses(warehouses: Iterable[WarehouseData]): JsValue = {
		fromIterable(warehouses)(fromWarehouse)
	}

}