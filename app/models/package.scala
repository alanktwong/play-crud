import util.{Try, Success, Failure}

package object models {


	def toFailure[T](msg: String): Try[T] = {
		Failure(new IllegalArgumentException(msg))
	}
}