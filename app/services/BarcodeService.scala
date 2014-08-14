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

trait BarcodeService extends misc.Logging {
	type BarcodeImage = Try[Array[Byte]]
	
	def renderImage(ean: Long): Future[BarcodeImage]

}

class BarcodeServiceImpl extends BarcodeService {
	lazy val barcodeCache: ActorRef = createBarcodeCache
	
	private def createBarcodeCache: ActorRef = {
		Akka.system.actorOf(Props[BarcodeCache])
	}
	
	def renderImage(ean: Long): Future[BarcodeImage] = {
		implicit val timeout = Timeout(20 seconds)
		barcodeCache ? RenderImage(ean) map {
			case RenderResult(result) => result
		}
	}
	
}

