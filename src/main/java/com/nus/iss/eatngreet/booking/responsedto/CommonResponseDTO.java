package com.nus.iss.eatngreet.booking.responsedto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class CommonResponseDTO {

	public String status;
	public String message;
	public String info;
	public boolean success;

	public CommonResponseDTO(String status, String message, String info, boolean success) {
		this.status = status;
		this.message = message;
		this.info = info;
		this.success = success;
	}
}
