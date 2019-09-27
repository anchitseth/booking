package com.nus.iss.eatngreet.booking.service;

import javax.servlet.http.HttpServletRequest;

import com.nus.iss.eatngreet.booking.requestdto.ConsumerOrderRequestDto;
import com.nus.iss.eatngreet.booking.requestdto.ProducerOrderRequestDto;
import com.nus.iss.eatngreet.booking.responsedto.CommonResponseDTO;

public interface BookingService {

	public CommonResponseDTO createProducerOrder(HttpServletRequest request, ProducerOrderRequestDto producerOrder);

	public CommonResponseDTO createConsumerOrder(HttpServletRequest request, ConsumerOrderRequestDto consumerOrder);

	public CommonResponseDTO fetchAllProducerOrders();

	public CommonResponseDTO fetchSingleItem(Long producerOrderId);

	public CommonResponseDTO fetchSingleConsumerItem(HttpServletRequest request);

	public CommonResponseDTO fetchSingleProducerItem(HttpServletRequest request);

}
