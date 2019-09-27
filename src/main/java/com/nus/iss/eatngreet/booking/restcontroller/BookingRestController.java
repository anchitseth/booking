package com.nus.iss.eatngreet.booking.restcontroller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nus.iss.eatngreet.booking.loggerservice.ApplicationLogger;
import com.nus.iss.eatngreet.booking.requestdto.ConsumerOrderRequestDto;
import com.nus.iss.eatngreet.booking.requestdto.ProducerOrderRequestDto;
import com.nus.iss.eatngreet.booking.responsedto.CommonResponseDTO;
import com.nus.iss.eatngreet.booking.service.BookingService;

@RestController
@RequestMapping("booking")
public class BookingRestController {

	@Autowired
	BookingService bookingService;

	@PostMapping("/producer-order")
	public CommonResponseDTO produceOrder(HttpServletRequest request,
			@RequestBody ProducerOrderRequestDto producerOrder) throws Exception {
		ApplicationLogger.logMessage("Application Started", BookingRestController.class);
		return bookingService.createProducerOrder(request, producerOrder);
	}

	@PostMapping("/consumer-order")
	public CommonResponseDTO consumeOrder(HttpServletRequest request,
			@RequestBody ConsumerOrderRequestDto consumerOrder) throws Exception {
		return bookingService.createConsumerOrder(request, consumerOrder);
	}

	@PostMapping("/all-items")
	public CommonResponseDTO fetchProducerOrder() {
		return bookingService.fetchAllProducerOrders();
	}

	@PostMapping("/single-producer-item")
	public CommonResponseDTO fetchSingleOrder(@RequestBody ConsumerOrderRequestDto consumerOrder) {
		return bookingService.fetchSingleItem(consumerOrder.getProducerOrderId());
	}

	@PostMapping("/user-consumer-item")
	public CommonResponseDTO fetchSingleConsumerOrder(HttpServletRequest request) {
		return bookingService.fetchSingleConsumerItem(request);
	}

	@PostMapping("/user-producer-item")
	public CommonResponseDTO fetchSingleProducerOrder(HttpServletRequest request) {
		return bookingService.fetchSingleProducerItem(request);
	}
	
	@GetMapping("/health-check")
	public String healthCheck() {
		return "Booking microservice is up and running.";
	}
}
