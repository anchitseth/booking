package com.nus.iss.eatngreet.booking.service;

import javax.servlet.http.HttpServletRequest;

import com.nus.iss.eatngreet.booking.requestdto.ConsumerOrderRequestDto;
import com.nus.iss.eatngreet.booking.requestdto.ProducerOrderRequestDto;
import com.nus.iss.eatngreet.booking.responsedto.CommonResponseDto;

public interface BookingService {

	public CommonResponseDto createProducerOrder(HttpServletRequest request, ProducerOrderRequestDto producerOrder);

	public CommonResponseDto createConsumerOrder(HttpServletRequest request, ConsumerOrderRequestDto consumerOrder);

	public CommonResponseDto fetchAllProducerOrders();

	public CommonResponseDto fetchSingleItem(Long producerOrderId);

	public CommonResponseDto fetchSingleConsumerItem(HttpServletRequest request);

	public CommonResponseDto fetchSingleProducerItem(HttpServletRequest request);

}
