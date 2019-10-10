package com.nus.iss.eatngreet.booking.restcontroller;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nus.iss.eatngreet.booking.requestdto.CreateMealRequestDto;
import com.nus.iss.eatngreet.booking.requestdto.GuestJoiningRequestDto;
import com.nus.iss.eatngreet.booking.responsedto.CommonResponseDto;
import com.nus.iss.eatngreet.booking.responsedto.DataResponseDto;
import com.nus.iss.eatngreet.booking.service.BookingService;

@RestController
@RequestMapping("booking")
public class BookingRestController {

	@Autowired
	BookingService bookingService;

	private static final Logger logger = LoggerFactory.getLogger(BookingRestController.class);

	@PostMapping("/producer-order")
	public CommonResponseDto createMeal(HttpServletRequest request,
			@RequestBody CreateMealRequestDto producerOrder) {
		logger.info("In produceOrder() of BookingRestController with request: {}.", producerOrder);
		return bookingService.createMeal(request, producerOrder);
	}

	@PostMapping("/consumer-order")
	public CommonResponseDto joinMeal(HttpServletRequest request, @RequestBody GuestJoiningRequestDto consumerOrder) {
		logger.info("In consumeOrder() of BookingRestController with request: {}.", consumerOrder);
		return bookingService.joinMeal(request, consumerOrder);
	}

	@PostMapping("/all-items")
	public DataResponseDto fetchAllMeals() {
		logger.info("In fetchProducerOrder() of BookingRestController.");
		return bookingService.fetchAllActiveMeals();
	}

	@PostMapping("/single-producer-item")
	public DataResponseDto fetchSingleMeal(@RequestBody GuestJoiningRequestDto consumerOrder) {
		logger.info("In fetchSingleOrder() of BookingRestController with request: {}", consumerOrder);
		return bookingService.fetchSingleMeal(consumerOrder.getProducerOrderId());
	}

	@PostMapping("/user-consumer-item")
	public DataResponseDto fetchAllJoinedMeals(HttpServletRequest request) {
		logger.info("In fetchSingleConsumerOrder() of BookingRestController.");
		return bookingService.fetchAllJoinedMealsOfUser(request);
	}

	@PostMapping("/user-producer-item")
	public DataResponseDto fetchAllHostedMeals(HttpServletRequest request) {
		logger.info("In fetchSingleProducerOrder() of BookingRestController.");
		return bookingService.fetchAllHostedMealsOfUser(request);
	}

	@GetMapping("/health-check")
	public String healthCheck() {
		logger.info("In healthCheck() of BookingRestController.");
		return "Booking microservice is up and running.";
	}
}
