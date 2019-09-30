package com.nus.iss.eatngreet.booking.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApplicationLogger {
	
	public static void logDebugMessage(String message, Class<?> className) {
		Logger logger = LoggerFactory.getLogger(className);
		logger.debug(message);
	}
	
	public static void logInfoMessage(String message, Class<?> className) {
		Logger logger = LoggerFactory.getLogger(ApplicationLogger.class);
		logger.info(message);
	}
	
	public static void logErrorMessage(String message, Class<?> className) {
		Logger logger = LoggerFactory.getLogger(className);
		logger.error(message);
	}
	
	public static void logWarningMessage(String message, Class<?> className) {
		Logger logger = LoggerFactory.getLogger(className);
		logger.warn(message);
	}
	
	private ApplicationLogger() {
		
	}
}
