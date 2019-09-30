package com.nus.iss.eatngreet.booking.util;

import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

public class Util {

	public static boolean isStringEmpty(String str) {
		return (str == null || str.trim().length() == 0);
	}

	public static boolean isStringOnlyAlphabets(String str) {
		return ((!str.equals("")) && (str != null) && (str.matches("^[a-zA-Z]*$")));
	}

	public static boolean isNumeric(String strNum) {
		try {
			Double.parseDouble(strNum);
		} catch (NumberFormatException | NullPointerException nfe) {
			return false;
		}
		return true;
	}

	public static boolean isListEmpty(List<?> list) {
		boolean isEmpty = true;
		if (list != null && !list.isEmpty()) {
			isEmpty = false;
		}
		return isEmpty;
	}

	public static boolean isLongValueEmpty(Long longValue) {
		boolean isEmpty = true;
		if (longValue != null && longValue > 0L) {
			isEmpty = false;
		}
		return isEmpty;
	}
	
	public static boolean isFloatValueEmpty(Float floatValue) {
		boolean isEmpty = true;
		if (floatValue != null && floatValue > 0L) {
			isEmpty = false;
		}
		return isEmpty;
	}

	public static Date getDateReducedByHours(Date dateToBeModified, Integer hours) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(dateToBeModified);
		calendar.add(Calendar.HOUR_OF_DAY, -hours);
		return calendar.getTime();
	}

	public static boolean isValidEmail(String email) {
		if (isStringEmpty(email))
			return false;
		String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\." + "[a-zA-Z0-9_+&*-]+)*@" + "(?:[a-zA-Z0-9-]+\\.)+[a-z"
				+ "A-Z]{2,7}$";
		Pattern pat = Pattern.compile(emailRegex);
		return pat.matcher(email).matches();
	}

	public static String getDecryptedEmail(HttpServletRequest request) {
		String authToken = request.getHeader("Authorization").substring("Basic".length()).trim();
		return new String(Base64.getDecoder().decode(authToken)).split(":")[0];
	}
	
	private Util() {
		
	}
}
