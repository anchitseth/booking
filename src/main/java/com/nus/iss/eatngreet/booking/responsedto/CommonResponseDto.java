package com.nus.iss.eatngreet.booking.responsedto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class CommonResponseDto {

	private String status;
	private String message;
	private String info;
	private boolean success;

	public CommonResponseDto(String status, String message, String info, boolean success) {
		this.status = status;
		this.message = message;
		this.info = info;
		this.success = success;
	}
}
