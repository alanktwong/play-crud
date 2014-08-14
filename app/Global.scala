import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.AnnotationConfigApplicationContext

import org.squeryl.PrimitiveTypeMode._

import util.{Try, Success, Failure}

import play.api._
import play.api.mvc._
import play.api.mvc.Results._
import play.api.Play.current
import play.api.libs.concurrent.Akka
import play.api.libs.concurrent.Execution.Implicits._

import models._
import misc.{Constants, SpringConfiguration}

object Global extends play.api.mvc.WithFilters(AccessLog) with GlobalSettings
{

	override def onStart(app: Application) {
		Logger.info("Starting up play-eventsourced")
		initSessionFactory(app)
		models.Database.initializeDomain()
	}
	
	private def initSessionFactory(implicit app: Application) = {
		import play.api.db.DB
		import org.squeryl.internals.DatabaseAdapter
		import org.squeryl.adapters.{H2Adapter, PostgreSqlAdapter}
		import org.squeryl.{Session, SessionFactory}
		import play.api.Play.current
		
		def getSession(app: Application, adapter: DatabaseAdapter): Session = {
			Session.create(DB.getConnection()(app), adapter)
		}
	
		SessionFactory.concreteFactory = app.configuration.getString("db.default.driver") match {
			case Some("org.h2.Driver") => Some(() => getSession(app, new H2Adapter))
			case Some("org.postgresql.Driver") => Some(() => getSession(app, new PostgreSqlAdapter))
			case _ =>
				sys.error("Database driver must be either org.h2.Driver or org.postgresql.Driver")
				None
		}
		SessionFactory
	}
	

//	override def onHandlerNotFound(request: RequestHeader) = {
//		Redirect(controllers.routes.Application.index())
//	}

	override def onStop(app: Application) = {
		Logger.info("Shutting down play-eventsourced")
		Akka.system.shutdown()
	}
	
	private var ctx: ApplicationContext = new AnnotationConfigApplicationContext(classOf[SpringConfiguration])

	
	override def getControllerInstance[A](clazz: Class[A]): A = {
		return ctx.getBean(clazz)
	}
}

object AccessLog extends Filter {
	import play.api.libs.concurrent.Execution.Implicits._
	import org.springframework.util.StopWatch
	
	def apply(next: (RequestHeader) => Result)(requestHeader: RequestHeader) = {
		val stopWatch = new org.springframework.util.StopWatch
		stopWatch.start()

		def logTime(result: PlainResult): Result = {
			stopWatch.stop()
			val time = stopWatch.getTotalTimeMillis()
			val status = result.header.status
			Logger.debug(String.format("%s %s took %s ms and returned %s", requestHeader.method, requestHeader.uri, time.toString, status.toString))
			result.withHeaders("Request-Time" -> time.toString)
		}
		next(requestHeader) match {
			case plain: PlainResult => {
				Logger.debug("requestHeader has plain result")
				logTime(plain)
			}
			case async: AsyncResult => {
				Logger.debug("requestHeader has async result")
				async.transform(logTime)
			}
		}
	}
}

