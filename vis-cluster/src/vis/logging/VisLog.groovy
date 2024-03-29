package vis.logging

import org.apache.log4j.ConsoleAppender
import org.apache.log4j.Level
import org.apache.log4j.Logger
import org.apache.log4j.PatternLayout

class VisLog {

	
	static void initLogging(){
		Logger logger = Logger.getRootLogger()
		logger.removeAllAppenders()
		
		ConsoleAppender appender = new ConsoleAppender(new PatternLayout("%d{ISO8601} [%-5p] [%c] %m %n"))
		logger.addAppender(appender)
		logger.setLevel(Level.INFO)
	}

	static main(args) {
	
	}

}
