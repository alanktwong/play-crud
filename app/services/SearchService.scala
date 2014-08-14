package services

import concurrent._
import concurrent.duration._

import akka.actor.{ActorRef, Props}
import akka.util.Timeout
import akka.pattern.ask

import play.api.libs.concurrent.Akka
import play.api.libs.concurrent.Execution.Implicits._
import play.api.Play.current

import services.actors._
import models._


/**
 * Not most elegant and specific name, but try to enforce
 * separation between command and query/search
 * per CQRS architecture
 * 
 */
trait SearchService extends misc.Logging {
	
	def findAll(): Seq[ProductData]
	def findByEan(ean: Long): Option[ProductData]
	def findProductsInWarehouseAndName(name: String, warehouse: WarehouseData): Seq[ProductData]
}

class SearchServiceImpl extends SearchService {
  
	def findAll(): Seq[ProductData] = {
		Product.findAll
	}
	def findByEan(ean: Long): Option[ProductData] = {
		Product.findByEan(ean)
	}
	def findProductsInWarehouseAndName(name: String, warehouse: WarehouseData): Seq[ProductData] = {
		Product.findProductsInWarehouseAndName(name, warehouse)
	}
	
	

}