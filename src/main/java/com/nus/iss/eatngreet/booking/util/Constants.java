package com.nus.iss.eatngreet.booking.util;

public class Constants {
	
	public static final Integer PAYMENT_DEADLINE_HOURS = 6;
	public static final String SUCCESS_STRING = "SUCCESS";
	public static final String FAILURE_STRING = "FAILURE";
	public static final String TRY_AGAIN_MSG_FOR_USERS = "Some error occurred, please try again later.";
	public static final String MEAL_POSTING_SUCCESS_MSG = "Meal successfully posted.";
	public static final String NEW_MEAL_NOTIFICATION_API_URL = "/eatngreet/notificationms/notify/confirm-posting-meal";
	public static final String PAY_FOR_MEAL_API_URL = "/eatngreet/paymentms/pay/now";
	public static final String GET_USERS_INFO_API_URL = "/eatngreet/userms/user/get-users-info";
	public static final String NO_ORDERS_EXIST = "No orders exist.";
	public static final String FIRST_NAME_KEY = "firstName";
	public static final String LAST_NAME_KEY = "lastName";
	public static final String EMAIL_IDS_KEY = "emailIds";
	public static final String USER_INFO_KEY = "userInfo";
	public static final String HOST_ORDERS_KEY = "producersOrder";
	public static final String AUTHORIZATION_HEADER_NAME = "Authorization";

	private Constants() {
		
	}
}
