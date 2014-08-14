package misc

import play.api.Play

import models._

object Constants {
	val EAN = "product.ean"
	val NAME = "product.name"
	val DESCRIPTION = "product.description"
	  
	val WH_CODE = "warehouse.code"
	val WH_NAME = "warehouse.name"
	  
	val STOCK_ITEM_CODE = "stockitem.code"
	val STOCK_ITEM_EAN  = "stockitem.ean"
	val STOCK_ITEM_QUANTITY  = "stockitem.quantity"

	lazy val MIME_TYPE = Play.current.configuration.getString("application.mimeType").getOrElse("image/png")
	
	lazy val IMAGE_RESOLUTION = Play.current.configuration.getInt("application.imageResolution").getOrElse(144)
	
	lazy val initialProducts: Seq[ProductData] = Vector(
		ProductData(5010255079763L, "Paperclips Large",
			"Large Plain Pack of 1000"),
		ProductData(5018206244666L, "Giant Paperclips",
			"Giant Plain 51mm 100 pack"),
		ProductData(5018306332812L, "Paperclip Giant Plain",
			"Giant Plain Pack of 10000"),
		ProductData(5018306312913L, "No Tear Paper Clip",
			"No Tear Extra Large Pack of 1000"),
		ProductData(5018206244611L, "Zebra Paperclips",
			"Zebra Length 28mm Assorted 150 Pack")
	)
	lazy val initialWarehouses: Seq[WarehouseData] = Vector(
		WarehouseData("W1", "Brooklyn"),
		WarehouseData("W2","LA")
	)
	
	def initialStockItem: Option[StockItemData] = {
		val maybeWarehouse = initialWarehousesMap.get("W1")
		val maybeProduct = initialProductsMap.get(5010255079763L)
		
		if (maybeWarehouse.isDefined && maybeProduct.isDefined) {
			Some(StockItemData.create(maybeProduct.get,maybeWarehouse.get, 123L))
		} else {
			None
		}
	}
	
	def initialProductsMap: Map[Long, ProductData] = {
		Map(initialProducts map { a => a.ean -> a }: _*)
	}

	def initialWarehousesMap: Map[String, WarehouseData] = {
		Map(initialWarehouses map { a => a.code -> a }: _*)
	}	
}