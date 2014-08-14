package misc

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers

import org.squeryl.PrimitiveTypeMode.inTransaction

import play.api.test._
import play.api.test.Helpers._

trait AbstractSpec extends ShouldMatchers with misc.Logging {
	def databaseTesting[T](block:  =>T): T = running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
		inTransaction {
			block
		}
	}
}

trait AbstractFlatSpec extends AbstractSpec with FlatSpec {
  
}