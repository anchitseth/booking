package com.nus.iss.eatngreet.booking.restcontroller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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

	@GetMapping("/items")
	public DataResponseDTO getItems(@RequestParam(name = "item") String item) {
		log.info("\ngetItems() of AutoSuggestRestController.");
		return autoSuggestService.getItems(item);
	}
	
	@GetMapping("/all-items")
	public DataResponseDTO getAllItems() {
		log.info("\ngetAllItems() of AutoSuggestRestController.");
		return autoSuggestService.getAllItems();
	}
	
	@PostMapping("/all-item-names")
	public DataResponseDTO getAllItemNames() {
		log.info("\ngetAllItems() of AutoSuggestRestController.");
		return autoSuggestService.getAllItemNames();
	}

}