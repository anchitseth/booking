package com.nus.iss.eatngreet.booking.restcontroller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nus.iss.eatngreet.booking.requestdto.ConsumerOrderRequestDto;
import com.nus.iss.eatngreet.booking.requestdto.ProducerOrderRequestDto;
import com.nus.iss.eatngreet.booking.responsedto.CommonResponseDTO;
import com.nus.iss.eatngreet.booking.service.BookingService;
import com.nus.iss.eatngreet.booking.util.ApplicationLogger;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("booking")
public class BookingRestController {

	@Autowired
	BookingService bookingService;

	// create an order by producer
	@PostMapping("/producer-order")
	public CommonResponseDTO produceOrder(HttpServletRequest request,
			@RequestBody ProducerOrderRequestDto producerOrder) throws Exception {
		log.info("In produceOrder() of BookingRestController with request: ");
		ApplicationLogger.logInfoMessage("In produceOrder() of BookingRestController with request: " + producerOrder,
				BookingRestController.class);
		return bookingService.createProducerOrder(request, producerOrder);
	}

	// create an order by consumer, i.e. consumer joins an existing order
	@PostMapping("/consumer-order")
	public CommonResponseDTO consumeOrder(HttpServletRequest request,
			@RequestBody ConsumerOrderRequestDto consumerOrder) throws Exception {
		ApplicationLogger.logInfoMessage("In consumeOrder() of BookingRestController with request: " + consumerOrder,
				BookingRestController.class);
		return bookingService.createConsumerOrder(request, consumerOrder);
	}

	@PostMapping("/all-items")
	public CommonResponseDTO fetchProducerOrder() {
		ApplicationLogger.logInfoMessage("In fetchProducerOrder() of BookingRestController.",
				BookingRestController.class);
		return bookingService.fetchAllProducerOrders();
	}

	@PostMapping("/single-producer-item")
	public CommonResponseDTO fetchSingleOrder(@RequestBody ConsumerOrderRequestDto consumerOrder) {
		ApplicationLogger.logInfoMessage("In fetchSingleOrder() of BookingRestController with request: " + consumerOrder,
				BookingRestController.class);
		return bookingService.fetchSingleItem(consumerOrder.getProducerOrderId());
	}

	// fetch all orders of a user as a consumer
	@PostMapping("/user-consumer-item")
	public CommonResponseDTO fetchSingleConsumerOrder(HttpServletRequest request) {
		ApplicationLogger.logInfoMessage(
				"In fetchSingleConsumerOrder() of BookingRestController with http request param.",
				BookingRestController.class);
		return bookingService.fetchSingleConsumerItem(request);
	}

	// fetch all orders of a user as a producer
	@PostMapping("/user-producer-item")
	public CommonResponseDTO fetchSingleProducerOrder(HttpServletRequest request) {
		ApplicationLogger.logInfoMessage(
				"In fetchSingleProducerOrder() of BookingRestController with http request param.",
				BookingRestController.class);
		return bookingService.fetchSingleProducerItem(request);
	}

	@GetMapping("/health-check")
	public String healthCheck() {
		ApplicationLogger.logInfoMessage("In healthCheck() of BookingRestController.", BookingRestController.class);
		return "Booking microservice is up and running.";
	}
}
