package com.nus.iss.eatngreet.booking.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.nus.iss.eatngreet.booking.entity.ItemEntity;
import com.nus.iss.eatngreet.booking.repository.ItemRepository;
import com.nus.iss.eatngreet.booking.responsedto.DataResponseDto;
import com.nus.iss.eatngreet.booking.service.AutoSuggestService;
import com.nus.iss.eatngreet.booking.util.Constants;
import com.nus.iss.eatngreet.booking.util.ResponseUtil;
import com.nus.iss.eatngreet.booking.util.Util;

@Service
public class AutoSuggestServiceImpl implements AutoSuggestService {

	@Autowired
	private ItemRepository itemRepository;
	
	private static final Logger logger = LoggerFactory.getLogger(AutoSuggestServiceImpl.class);


	public DataResponseDto getMatchingItems(String matchingString) {
		logger.info("getMatchingItems() of AutoSuggestServiceImpl. With string: {}.", matchingString);
		DataResponseDto response = new DataResponseDto();
		int count = 5;
		if (Util.isStringEmpty(matchingString)) {
			response.setData(null);
			logger.warn("String entered by user is either empty or null.");
			ResponseUtil.prepareResponse(response, "String can't be empty or null.", Constants.FAILURE_STRING,
					"String entered by user is either empty or null.", false);
		} else {
			try {
				List<ItemEntity> items = itemRepository.itemNameAutoSuggest(matchingString, PageRequest.of(0, count));
				if (!Util.isListEmpty(items)) {
					HashMap<Object, Object> data = new HashMap<>();
					List<Object> itemsList = new ArrayList<>();
					for (ItemEntity item : items) {
						HashMap<String, Object> record = new HashMap<>();
						record.put("id", item.getItemId());
						record.put("name", item.getName());
						record.put("cuisine", item.getCuisine());
						record.put("description", item.getDescription());
						itemsList.add(record);
					}
					data.put("items", itemsList);
					response.setData(data);
					logger.info("Successfully fetched matching items for string: {}.", matchingString);
					ResponseUtil.prepareResponse(response, Constants.SUCCESS_STRING, Constants.SUCCESS_STRING,
							Constants.SUCCESS_STRING, true);
				} else {
					logger.info("No matching items found for string: {}", matchingString);
					ResponseUtil.prepareResponse(response, "No items found for autosuggest.", Constants.SUCCESS_STRING,
							Constants.SUCCESS_STRING, true);
				}
			} catch (Exception e) {
				response.setData(null);
				logger.error("Exception occurred: {}.", e.getMessage());
				ResponseUtil.prepareResponse(response, Constants.TRY_AGAIN_MSG_FOR_USERS, Constants.FAILURE_STRING,
						e.getMessage(), false);
			}
		}
		return response;
	}

	@Override
	public DataResponseDto getAllItems() {
		logger.info("getAllItems() of AutoSuggestServiceImpl.");
		DataResponseDto response = new DataResponseDto();
		try {
			List<ItemEntity> items = itemRepository.findAll();
			if (!Util.isListEmpty(items)) {
				HashMap<Object, Object> data = new HashMap<>();
				List<Object> itemsList = new ArrayList<>();
				for (ItemEntity item : items) {
					HashMap<String, Object> record = new HashMap<>();
					record.put("id", item.getItemId());
					record.put("name", item.getName());
					record.put("cuisine", item.getCuisine());
					record.put("description", item.getDescription());
					itemsList.add(record);
				}
				data.put("items", itemsList);
				response.setData(data);
				logger.info(Constants.SUCCESS_STRING);
				ResponseUtil.prepareResponse(response, Constants.SUCCESS_STRING, Constants.SUCCESS_STRING, "Successful execution.",
						true);
			} else {
				logger.info("No items found in the item table.");
				ResponseUtil.prepareResponse(response, "No items found.", Constants.SUCCESS_STRING,
						"Item table is empty.", true);
			}
		} catch (Exception e) {
			response.setData(null);
			logger.error("Exception occurred while trying to fetch all items. Exception msg: {}.", e.getMessage());
			ResponseUtil.prepareResponse(response, Constants.TRY_AGAIN_MSG_FOR_USERS, Constants.FAILURE_STRING,
					e.getMessage(), false);
		}
		return response;
	}

	@Override
	public DataResponseDto getAllItemNames() {
		logger.info("getAllItemNames() of AutoSuggestServiceImpl.");
		DataResponseDto response = new DataResponseDto();
		try {
			List<ItemEntity> items = itemRepository.findAll();
			if (!Util.isListEmpty(items)) {
				HashMap<Object, Object> data = new HashMap<>();
				Set<String> itemsList = new HashSet<>();
				for (ItemEntity item : items) {
					itemsList.add(item.getName());
				}
				data.put("items", itemsList);
				response.setData(data);
				logger.info(Constants.SUCCESS_STRING);
				ResponseUtil.prepareResponse(response, Constants.SUCCESS_STRING, Constants.SUCCESS_STRING, "Successful execution.",
						true);
			} else {
				logger.warn("No items present in the item table.");
				ResponseUtil.prepareResponse(response, "No matching items found.", Constants.SUCCESS_STRING,
						"No items present in the item table.", true);
			}
		} catch (Exception e) {
			response.setData(null);
			logger.error("Exception occurred while trying to fetch all item names. Exception message: {}.", e.getMessage());
			ResponseUtil.prepareResponse(response, Constants.TRY_AGAIN_MSG_FOR_USERS, Constants.FAILURE_STRING,
					e.getMessage(), false);
		}
		return response;
	}

}