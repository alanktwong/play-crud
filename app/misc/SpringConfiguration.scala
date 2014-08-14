package misc

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Bean


@Configuration
class SpringConfiguration {

	
	@Bean
	def barcodeService: services.BarcodeService = {
		play.api.Logger.info("Initializing BarcodeService")
		new services.BarcodeServiceImpl
	}
	@Bean
	def searchService: services.SearchService = {
		play.api.Logger.info("Initializing SearchService")
		new services.SearchServiceImpl
	}
	@Bean
	def commandService: services.CommandService = {
		play.api.Logger.info("Initializing CommandService")
		new services.CommandServiceImpl
	}
	
	@Bean
	def application: controllers.Application = new controllers.Application
	
	@Bean
	def barcodes: controllers.Barcodes = new controllers.Barcodes(barcodeService)
	
	@Bean
	def products: controllers.Products = new controllers.Products
	@Bean
	def warehouses: controllers.Warehouses = new controllers.Warehouses
	@Bean
	def stockItems: controllers.StockItems = new controllers.StockItems

}