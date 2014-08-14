package services.actors
import akka.actor.{Actor, ActorLogging}
import misc.Constants
import concurrent._
import concurrent.duration._
import scala.util.Try
import play.api.libs.concurrent.Execution.Implicits._
import akka.actor.actorRef2Scala
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import org.krysalis.barcode4j.impl.upcean.EAN13Bean
import org.krysalis.barcode4j.output.bitmap.BitmapCanvasProvider
import play.api.Play.current
import play.api.cache.Cache


class BarcodeCache extends Actor with ActorLogging {
	
	import play.api.cache.Cache
	import play.api.Play.current
	
	type FutureImage = Future[Array[Byte]]
	// replace with import play.api.cache.Cache

	//var imageCache = Map[Long, FutureImage]()
	private def cacheKey(ean: Long): String = "barcode-" + ean 
	def receive: Receive = {
		case RenderImage(ean) =>
			
			val futureImage = Cache.getAs[FutureImage](cacheKey(ean)) match {
				case Some(futureImage) =>
					log.info("barcode cache hit on " + ean)
					futureImage
				case None => {
					log.info("barcode cache miss on " + ean)
					val futureImage = future { ean13BarCode(ean, Constants.MIME_TYPE) }
					log.info("barcode cache put on " + ean)
					Cache.set(cacheKey(ean), futureImage, 1800)
					futureImage
				}
			}
			
			val client = sender
			futureImage onComplete {
				client ! RenderResult(_)
			}
	}
	
	private def ean13BarCode(ean: Long, mimeType: String): Array[Byte] = {
		import java.io.ByteArrayOutputStream
		import java.awt.image.BufferedImage
		import org.krysalis.barcode4j.output.bitmap.BitmapCanvasProvider
		import org.krysalis.barcode4j.impl.upcean.EAN13Bean

		var output: ByteArrayOutputStream = new ByteArrayOutputStream
		val imageResolution = Constants.IMAGE_RESOLUTION
		var canvas: BitmapCanvasProvider =
			new BitmapCanvasProvider(output, mimeType, imageResolution,
				BufferedImage.TYPE_BYTE_BINARY, false, 0)

		val barCode = new EAN13Bean()
		barCode.generateBarcode(canvas, String valueOf ean)
		canvas.finish

		output.toByteArray
	}
}


case class RenderImage(ean: Long)
case class RenderResult(image: Try[Array[Byte]])