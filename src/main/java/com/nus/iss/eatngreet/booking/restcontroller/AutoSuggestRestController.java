package com.nus.iss.eatngreet.booking.restcontroller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nus.iss.eatngreet.booking.responsedto.DataResponseDTO;
import com.nus.iss.eatngreet.booking.service.AutoSuggestService;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/auto-suggest")
public class AutoSuggestRestController {

	@Autowired
	private AutoSuggestService autoSuggestService;

	@RequestMapping(method = RequestMethod.GET, value = "/items")
	public DataResponseDTO getItems(@RequestParam(name = "item") String item) {
		log.info("\ngetItems() of AutoSuggestRestController.");
		return autoSuggestService.getItems(item);
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/all-items")
	public DataResponseDTO getAllItems() {
		log.info("\ngetAllItems() of AutoSuggestRestController.");
		return autoSuggestService.getAllItems();
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/all-item-names")
	public DataResponseDTO getAllItemNames() {
		log.info("\ngetAllItems() of AutoSuggestRestController.");
		return autoSuggestService.getAllItemNames();
	}

}