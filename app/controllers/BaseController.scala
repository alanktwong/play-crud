package controllers

import play.api.mvc._
import play.api.i18n.Lang

trait BaseController extends ControllerActions

case class ViewContext(request: Request[AnyContent], lang: Lang, maybeUser: Option[models.User]) extends WrappedRequest(request)

trait ControllerActions extends Controller with misc.Logging {

	type ResultOrCredentials = Option[Either[Result, (String,String)]]
	
	def ViewContextAction(f: ViewContext => Result): Action[AnyContent] = {
		Action { implicit request =>
			f(ViewContext(request, lang, getUser(request)))
		}
	}
	
	def AuthenticatedAction(f: ViewContext => Result): Action[AnyContent] = {
		ViewContextAction { implicit context =>
			maybeCredentials(context) map { resultOrCredentials =>
				resultOrCredentials match {
					case Left(errorResult) => errorResult
					case Right(credentials) =>
						val (username, password) = credentials
						if (authenticate(username, password)) {
							f(context)
						} else {
							Unauthorized
						}
				}
			  
			  
			} getOrElse {
				Unauthorized("No user name and password provided")
			}
			
		}
	}
	
	def getUser(request: Request[AnyContent]): Option[models.User] = {
		None
	}
	def authenticate[A](username: String, password: String)(implicit request: Request[AnyContent]): Boolean = {
		logger.info(s"Authenticating (${username},${password})")
		true
	}
	
	protected def maybeCredentials(request: Request[AnyContent]): ResultOrCredentials = {
		readQueryString(request) orElse readBasicAuthentication(request.headers)
	}
	
	protected def readBasicAuthentication(headers: Headers): ResultOrCredentials = {
		headers.get(play.mvc.Http.HeaderNames.AUTHORIZATION) map { header =>
		 	val BasicHeader = "Basic (.*)".r
		 	header match {
				case BasicHeader(base64) => {
					try {
						val decodedBytes = org.apache.commons.codec.binary.Base64.decodeBase64(base64.getBytes)
						val credentials = new String(decodedBytes).split(":",2)
						credentials match {
							case Array(username, password) =>
								Right(username, password)
							case _ =>
								Left(BadRequest("Invalid authorization header"))
						}
					} catch {
						case th: Throwable => Left(BadRequest("Invalid authorization header"))
					}
				}
				case _ => Left(BadRequest("Bad authorization header"))
		 	}
		}
	}
	
	protected def readQueryString(request: Request[AnyContent]): ResultOrCredentials = {
		request.queryString.get("user").map{ username =>
			request.queryString.get("password").map { password =>
				Right( (username.head, password.head) )
			}.getOrElse{
				Left(BadRequest("Password not specified"))
			}
		}
	}
}

