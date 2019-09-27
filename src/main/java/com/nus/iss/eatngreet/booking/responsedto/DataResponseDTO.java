package com.nus.iss.eatngreet.booking.responsedto;

import java.util.Map;

@SuppressWarnings("rawtypes")
public class DataResponseDTO extends CommonResponseDTO {

	private Map data;

	public Map getData() {
		return data;
	}

	public void setData(Map data) {
		this.data = data;
	}

}
