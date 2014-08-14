import play.api.mvc._

package object controllers {
	/** An alias for the `Nothing` type.
	 *	Denotes that the type should be filled in.
	 */
	type ??? = Nothing

	/** An alias for the `Any` type.
	 *	Denotes that the type should be filled in.
	 */
	type *** = Any
	
	
	trait BaseController extends play.api.mvc.Controller with misc.Logging {
	
	}
}