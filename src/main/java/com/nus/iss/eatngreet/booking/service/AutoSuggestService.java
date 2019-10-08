package com.nus.iss.eatngreet.booking.service;

import com.nus.iss.eatngreet.booking.responsedto.DataResponseDto;

public interface AutoSuggestService {
	
	public DataResponseDto getItems(String itemName);

	public DataResponseDto getAllItems();

	public DataResponseDto getAllItemNames();

}
