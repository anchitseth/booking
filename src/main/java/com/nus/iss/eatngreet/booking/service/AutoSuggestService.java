package com.nus.iss.eatngreet.booking.service;

import com.nus.iss.eatngreet.booking.responsedto.DataResponseDTO;

public interface AutoSuggestService {
	
	public DataResponseDTO getItems(String itemName);

	public DataResponseDTO getAllItems();

	public DataResponseDTO getAllItemNames();

}
