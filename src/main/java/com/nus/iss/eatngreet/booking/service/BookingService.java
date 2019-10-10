package com.nus.iss.eatngreet.booking.service;

import javax.servlet.http.HttpServletRequest;

import com.nus.iss.eatngreet.booking.requestdto.CreateMealRequestDto;
import com.nus.iss.eatngreet.booking.requestdto.GuestJoiningRequestDto;
import com.nus.iss.eatngreet.booking.responsedto.CommonResponseDto;
import com.nus.iss.eatngreet.booking.responsedto.DataResponseDto;

public interface BookingService {

	public CommonResponseDto createMeal(HttpServletRequest request, CreateMealRequestDto producerOrder);

	public CommonResponseDto joinMeal(HttpServletRequest request, GuestJoiningRequestDto consumerOrder);

	public DataResponseDto fetchAllActiveMeals();

	public DataResponseDto fetchSingleMeal(Long producerOrderId);

	public DataResponseDto fetchAllJoinedMealsOfUser(HttpServletRequest request);

	public DataResponseDto fetchAllHostedMealsOfUser(HttpServletRequest request);

}
