package com.nus.iss.eatngreet.booking.restcontroller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nus.iss.eatngreet.booking.responsedto.DataResponseDto;
import com.nus.iss.eatngreet.booking.service.AutoSuggestService;

@RestController
@RequestMapping("/auto-suggest")
public class AutoSuggestRestController {

	@Autowired
	private AutoSuggestService autoSuggestService;
	
	private static final Logger logger = LoggerFactory.getLogger(AutoSuggestRestController.class);

	@GetMapping("/items")
	public DataResponseDto getMatchingItems(@RequestParam(name = "item") String item) {
		logger.info("getMatchingItems() of AutoSuggestRestController, matching string: {}.", item);
		return autoSuggestService.getMatchingItems(item);
	}
	
	@GetMapping("/all-items")
	public DataResponseDto getAllItems() {
		logger.info("getAllItems() of AutoSuggestRestController.");
		return autoSuggestService.getAllItems();
	}
	
	@PostMapping("/all-item-names")
	public DataResponseDto getAllItemNames() {
		logger.info("getAllItemNames() of AutoSuggestRestController.");
		return autoSuggestService.getAllItemNames();
	}

}