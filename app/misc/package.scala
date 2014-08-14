import org.slf4j.LoggerFactory
import org.slf4j.Logger

package object misc {
	trait Logging {
		lazy val logger = LoggerFactory.getLogger(getClass)
	
		implicit def logging2Logger(anything: Logging): Logger = anything.logger
	}

}