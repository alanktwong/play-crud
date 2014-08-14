package services

import concurrent._
import concurrent.duration._
import scala.util.Try

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
trait CommandService extends misc.Logging {
	def save(data: ProductData): Try[ProductData]
	def delete(data: ProductData): Try[ProductData]

}

class CommandServiceImpl extends CommandService {
	def save(data: ProductData): Try[ProductData] = {
		Product.save(data)
	}
	def delete(data: ProductData): Try[ProductData] = {
		Product.delete(data)
	}
}
