package com.nus.iss.eatngreet.booking.util;

import com.nus.iss.eatngreet.booking.responsedto.CommonResponseDTO;

public class ResponseUtil {

	public static void prepareResponse(CommonResponseDTO response, String message, String status, String info,
			boolean success) {
		if (response != null) {
			response.setMessage(message);
			response.setStatus(status);
			response.setSuccess(success);
			response.setInfo(info);
		}
	}
	
	private ResponseUtil() {
		
	}
}
