package com.nus.iss.eatngreet.booking.loggerservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApplicationLogger {
	@SuppressWarnings("rawtypes")
	public static void logMessage(String message, Class className) {
		Logger logger = LoggerFactory.getLogger("com.nus.iss.eatngreet.booking");
		logger.debug(message);
	}
}
