package com.nus.iss.eatngreet.booking.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.nus.iss.eatngreet.booking.entity.ConsumerOrderEntity;
import com.nus.iss.eatngreet.booking.entity.ItemEntity;
import com.nus.iss.eatngreet.booking.entity.ProducerOrderEntity;
import com.nus.iss.eatngreet.booking.repository.ConsumerOrderRepository;
import com.nus.iss.eatngreet.booking.repository.ItemRepository;
import com.nus.iss.eatngreet.booking.repository.ProducerOrderRepository;
import com.nus.iss.eatngreet.booking.requestdto.ConsumerOrderRequestDto;
import com.nus.iss.eatngreet.booking.requestdto.ProducerOrderRequestDto;
import com.nus.iss.eatngreet.booking.responsedto.CommonResponseDto;
import com.nus.iss.eatngreet.booking.responsedto.DataResponseDTO;
import com.nus.iss.eatngreet.booking.responsedto.MyConsumerOrdersResponseDto;
import com.nus.iss.eatngreet.booking.responsedto.MyProducerOrdersResponseDto;
import com.nus.iss.eatngreet.booking.responsedto.ProducerOrderResponseDto;
import com.nus.iss.eatngreet.booking.responsedto.ProducerOrdersListingResponseDto;
import com.nus.iss.eatngreet.booking.service.BookingService;
import com.nus.iss.eatngreet.booking.util.ApplicationLogger;
import com.nus.iss.eatngreet.booking.util.Constants;
import com.nus.iss.eatngreet.booking.util.ResponseUtil;
import com.nus.iss.eatngreet.booking.util.Util;

@Service
public class BookingServiceImpl implements BookingService {

	@Autowired
	ItemRepository itemRepository;

	@Autowired
	ProducerOrderRepository producerOrderRepository;

	@Autowired
	ConsumerOrderRepository consumerOrderRepository;

	@Value("${eatngreet.usermicroservice.url.domain}")
	private String userMicroserviceDomain;

	@Value("${eatngreet.usermicroservice.url.port}")
	private String userMicroservicePort;

	@Value("${eatngreet.paymentmicroservice.url.domain}")
	private String paymentMicroserviceDomain;

	@Value("${eatngreet.paymentmicroservice.url.port}")
	private String paymentMicroservicePort;

	@Value("${eatngreet.notificationmicroservice.url.domain}")
	private String notificationMicroserviceDomain;

	@Value("${eatngreet.notificationmicroservice.url.port}")
	private String notificationMicroservicePort;

	@Value("${notificationmicroservice.email.auth.token}")
	private String emailAuthToken;

	@SuppressWarnings("unchecked")
	@Override
	public CommonResponseDto createProducerOrder(HttpServletRequest request, ProducerOrderRequestDto producerOrder) {
		CommonResponseDto response = new CommonResponseDto();
		try {
			ApplicationLogger.logInfoMessage(
					"In createProducerOrder() of BookingServiceImpl with request: " + producerOrder,
					BookingServiceImpl.class);
			if (checkProducerOrderRequestObj(producerOrder, response)) {
				ProducerOrderEntity producerOrderEntity = createProducerOrderEntity(Util.getDecryptedEmail(request),
						producerOrder);
				producerOrderRepository.save(producerOrderEntity);
				ResponseUtil.prepareResponse(response, "Successfully created producer order.", Constants.SUCCESS_STRING,
						"Successfully created producer order.", true);
				List<ProducerOrderEntity> producerOrders = new ArrayList<>();
				producerOrders.add(producerOrderEntity);
				DataResponseDTO userDetailsResponse = getProducersNameAndAddress(producerOrders);
				if (userDetailsResponse.isSuccess()) {
					Map<String, Object> userResponse = userDetailsResponse.getData();
					Map<String, Object> info = (Map<String, Object>) userResponse.get("userInfo");
					Map<String, Object> producerDetails = (Map<String, Object>) info
							.get(producerOrderEntity.getEmail());
					String name = producerDetails.get("firstName") + " " + producerDetails.get("lastName");
					CommonResponseDto notificationResponse = sendConfirmationEmail(producerOrderEntity, name);
					if (notificationResponse.isSuccess()) {
						ResponseUtil.prepareResponse(response, "Successfully created producer order.",
								Constants.SUCCESS_STRING,
								"Successfully created producer order and successfully sent the required mail.", true);
					} else {
						ResponseUtil.prepareResponse(response, "Successfully created producer order.",
								Constants.SUCCESS_STRING,
								"Successfully created producer order but notification mail was not sent. Reason:"
										+ notificationResponse.getInfo(),
								true);
					}

				} else {
					ResponseUtil.prepareResponse(response, "Successfully created producer order.",
							Constants.SUCCESS_STRING,
							"Some problem occurred in fetching producers name and address for sending the email, hence email for this was not sent. Info: "
									+ userDetailsResponse.getInfo(),
							false);
				}
			}
		} catch (Exception ex) {
			ResponseUtil.prepareResponse(response, "Please try again later.", Constants.FAILURE_STRING,
					"Following exception occurred while trying to create producer order: " + ex.getMessage(), false);
		}
		return response;
	}

	private CommonResponseDto sendConfirmationEmail(ProducerOrderEntity order, String name) {
		Map<String, Object> notificationReqMap = new HashMap<>();
		notificationReqMap.put("name", name);
		notificationReqMap.put("servingDate", order.getServingDate());
		notificationReqMap.put("count", order.getMaxPeopleCount());
		notificationReqMap.put("email", order.getEmail());
		final String uri = notificationMicroserviceDomain + ":" + notificationMicroservicePort
				+ "/eatngreet/notificationms/notify/confirm-posting-meal";
		HttpHeaders headers = new HttpHeaders();
		headers.set("Content-Type", "application/json");
		headers.set("Authorization", "Bearer " + emailAuthToken);
		HttpEntity<Map<String, Object>> entity = new HttpEntity<>(notificationReqMap, headers);
		RestTemplate restTemplate = new RestTemplate();
		return restTemplate.postForObject(uri, entity, CommonResponseDto.class);
	}

	private ProducerOrderEntity createProducerOrderEntity(String consumerEmailId,
			ProducerOrderRequestDto producerOrder) {
		ProducerOrderEntity producerOrderEntity = new ProducerOrderEntity();
		if (Util.isValidEmail(consumerEmailId)) {
			producerOrderEntity.setEmail(consumerEmailId);
		}
		producerOrderEntity.setPaymentDeadline(calculatePaymentDeadline(producerOrder.getReservationDeadline()));
		producerOrderEntity.setMaxPeopleCount(producerOrder.getMaxPeopleCount());
		producerOrderEntity.setPreferenceType(producerOrder.getPreferenceType());
		producerOrderEntity.setPrice(producerOrder.getPrice());
		producerOrderEntity.setReservationDeadline(producerOrder.getReservationDeadline());
		producerOrderEntity.setServingDate(producerOrder.getServingDate());
		producerOrderEntity.setOtherItems(producerOrder.getOtherItems());
		producerOrderEntity.setNote(producerOrder.getNote());
		producerOrderEntity.setIsActive(true);
		producerOrderEntity.setIsDeleted(false);
		Set<ItemEntity> itemSet = getItems(producerOrder);
		if (!itemSet.isEmpty()) {
			producerOrderEntity.setItemList(itemSet);
		}
		return producerOrderEntity;
	}

	private Set<ItemEntity> getItems(ProducerOrderRequestDto producerOrder) {
		Set<ItemEntity> itemSet = new HashSet<>();
		for (String itemName : producerOrder.getItemList()) {
			if (!Util.isStringEmpty(itemName)) {
				List<ItemEntity> itemList = itemRepository.findByName(itemName);
				if (!itemList.isEmpty()) {
					itemSet.add(itemList.get(0));
				}
			}
		}
		return itemSet;
	}

	private boolean checkProducerOrderRequestObj(ProducerOrderRequestDto producerOrder,
			CommonResponseDto commonResponseDTO) {
		if (Util.isListEmpty(producerOrder.getItemList())) {
			ResponseUtil.prepareResponse(commonResponseDTO, "Select one or more items to proceed.",
					Constants.FAILURE_STRING, "No Items Selected ", false);
			return false;
		} else if (producerOrder.getMaxPeopleCount() == null
				|| Util.isLongValueEmpty(producerOrder.getMaxPeopleCount())) {
			ResponseUtil.prepareResponse(commonResponseDTO, "Max people count is required parameter.",
					Constants.FAILURE_STRING, "Max People Count not set.", false);
			return false;
		} else if (producerOrder.getPreferenceType() == null || Util.isFloatValueEmpty(producerOrder.getPrice())) {
			ResponseUtil.prepareResponse(commonResponseDTO, "Select whether to Dine in or Take away.",
					Constants.FAILURE_STRING, "Preference type not selected", false);
			return false;
		} else if (producerOrder.getReservationDeadline() == null) {
			ResponseUtil.prepareResponse(commonResponseDTO, "Reservation deadline not set.", Constants.FAILURE_STRING,
					"Reservation deadline not set.", false);
			return false;
		} else if (producerOrder.getServingDate() == null) {
			ResponseUtil.prepareResponse(commonResponseDTO, "Serving date not set.", Constants.FAILURE_STRING,
					"Serving date not set.", false);
			return false;
		}
		return true;
	}

	private Date calculatePaymentDeadline(Date reservationDeadline) {
		return Util.getDateReducedByHours(reservationDeadline, Constants.PAYMENT_DEADLINE_HOURS);
	}

	@Override
	public CommonResponseDto createConsumerOrder(HttpServletRequest request, ConsumerOrderRequestDto consumerOrder) {
		CommonResponseDto response = new CommonResponseDto();
		try {
			ApplicationLogger.logInfoMessage("Starting BookingServiceImpl for createConsumerOrder",
					BookingServiceImpl.class);
			if (!Util.isLongValueEmpty(consumerOrder.getProducerOrderId())) {
				String consumerEmailId = Util.getDecryptedEmail(request);
				ConsumerOrderEntity consumerOrderEntity = createConsumerOrderEntity(consumerEmailId, consumerOrder,
						response);
				if (consumerOrderEntity != null) {
					Float amount = consumerOrderEntity.getProducerOrderEntity().getPrice();
					Map<String, Object> txnReqMap = new HashMap<>();
					txnReqMap.put("consumerEmailId", consumerEmailId);
					txnReqMap.put("producerEmailId", consumerOrderEntity.getProducerOrderEntity().getEmail());
					txnReqMap.put("producerOrderId", consumerOrderEntity.getProducerOrderEntity().getProducerOrderId());
					txnReqMap.put("amount", amount);
					final String uri = paymentMicroserviceDomain + ":" + paymentMicroservicePort
							+ "/eatngreet/paymentms/pay/now";
					HttpHeaders headers = new HttpHeaders();
					headers.set("Content-Type", "application/json");
					HttpEntity<Map<String, Object>> entity = new HttpEntity<>(txnReqMap, headers);
					RestTemplate restTemplate = new RestTemplate();
					CommonResponseDto paymentResponse = restTemplate.postForObject(uri, entity,
							CommonResponseDto.class);
					if (paymentResponse != null && paymentResponse.isSuccess()) {
						consumerOrderRepository.save(consumerOrderEntity);
						ResponseUtil.prepareResponse(response, "Successfully joined the meal.",
								Constants.SUCCESS_STRING, "Customer has successfully joined the meal.", true);
						CommonResponseDto notificationResponse = sendJoiningEmail(consumerOrderEntity);
						if (notificationResponse.isSuccess()) {
							ResponseUtil.prepareResponse(response, "Successfully created producer order.",
									Constants.SUCCESS_STRING,
									"Successfully created producer order and successfully sent the required mail.",
									true);
						} else {
							ResponseUtil.prepareResponse(response, "Successfully created producer order.",
									Constants.SUCCESS_STRING,
									"Successfully created producer order but notification mail was not sent. Reason:"
											+ notificationResponse.getInfo(),
									true);
						}
					} else if (paymentResponse == null) {
						ResponseUtil.prepareResponse(response, "Couldn't process payment. Please try again later.",
								Constants.FAILURE_STRING, "Couldn't process payment. Null returned from pay now api.",
								false);
					} else {
						ResponseUtil.prepareResponse(response,
								"Couldn't process payment. " + paymentResponse.getMessage(), Constants.FAILURE_STRING,
								"Couldn't process payment (as per the response from Payment Microservice). Response: "
										+ paymentResponse.getMessage(),
								false);
					}
				}
			} else {
				ResponseUtil.prepareResponse(response, "Producer order Id missing", Constants.FAILURE_STRING,
						"Producer order Id missing", false);
			}
		} catch (Exception ex) {
			ResponseUtil.prepareResponse(response, "Not Registered Successfully", Constants.FAILURE_STRING,
					"Following exception occurred " + ex.getMessage(), false);
		}
		return response;
	}

	@SuppressWarnings("unchecked")
	private CommonResponseDto sendJoiningEmail(ConsumerOrderEntity order) {
		CommonResponseDto response = new CommonResponseDto();
		Map<String, Object> notificationReqMap = new HashMap<>();
		Set<String> emailIds = new HashSet<>();
		emailIds.add(order.getEmail());
		emailIds.add(order.getProducerOrderEntity().getEmail());
		Map<String, Set<String>> emailIdMap = new HashMap<>();
		emailIdMap.put("emailIds", emailIds);
		final String uri = userMicroserviceDomain + ":" + userMicroservicePort
				+ "/eatngreet/userms/user/get-users-info";
		HttpHeaders headers = new HttpHeaders();
		headers.set("Content-Type", "application/json");
		HttpEntity<Map<String, Set<String>>> userInfoReqEntity = new HttpEntity<>(emailIdMap, headers);
		RestTemplate restTemplate = new RestTemplate();
		DataResponseDTO userResponse = restTemplate.postForObject(uri, userInfoReqEntity, DataResponseDTO.class);
		if (userResponse.isSuccess()) {
			Map<String, Object> details = (Map<String, Object>) userResponse.getData().get("userInfo");
			Map<String, Object> hostDetails = (Map<String, Object>) details
					.get(order.getProducerOrderEntity().getEmail());
			Map<String, Object> guestDetails = (Map<String, Object>) details.get(order.getEmail());
			notificationReqMap.put("hostName", hostDetails.get("firstName") + " " + hostDetails.get("lastName"));
			notificationReqMap.put("guestName", guestDetails.get("firstName") + " " + guestDetails.get("lastName"));
			notificationReqMap.put("hostEmailId", order.getProducerOrderEntity().getEmail());
			notificationReqMap.put("guestEmailId", order.getEmail());
			notificationReqMap.put("servingDate", order.getProducerOrderEntity().getServingDate());
			notificationReqMap.put("maxCount", order.getProducerOrderEntity().getMaxPeopleCount());
			notificationReqMap.put("guestCount", order.getProducerOrderEntity().getActualPeopleCount());
			final String notificationUri = notificationMicroserviceDomain + ":" + notificationMicroservicePort
					+ "/eatngreet/notificationms/notify/confirm-joining-meal";
			headers = new HttpHeaders();
			headers.set("Content-Type", "application/json");
			headers.set("Authorization", "Bearer " + emailAuthToken);
			HttpEntity<Map<String, Object>> entity = new HttpEntity<>(notificationReqMap, headers);
			restTemplate = new RestTemplate();
			CommonResponseDto notificationResponse = restTemplate.postForObject(notificationUri, entity, CommonResponseDto.class);
			if(notificationResponse.isSuccess()) {

				ResponseUtil.prepareResponse(response, "Mail sent successfully.", Constants.SUCCESS_STRING,
						"Mail sent successfully.", true);
			} else {
				ResponseUtil.prepareResponse(response, "Mail was not sent.", Constants.FAILURE_STRING,
						"Mail was not sent. Reason: " + notificationResponse.getInfo(), false);
			}
		} else {
			ResponseUtil.prepareResponse(response, "Unable to fetch user info.", Constants.FAILURE_STRING,
					"Unable to fetch user info. Reason: " + userResponse.getInfo(), false);
		}
		return response;

		// get user details

	}

	private ConsumerOrderEntity createConsumerOrderEntity(String consumerEmailId, ConsumerOrderRequestDto consumerOrder,
			CommonResponseDto commonResponseDTO) {
		ConsumerOrderEntity consumerOrderEntity = null;
		List<ProducerOrderEntity> producerOrderEntityList = producerOrderRepository
				.findByProducerOrderId(consumerOrder.getProducerOrderId());
		if (!producerOrderEntityList.isEmpty()) {
			ProducerOrderEntity producerOrderEntity = producerOrderEntityList.get(0);
			if (checkPeopleCount(producerOrderEntity)) {
				if (Util.isValidEmail(consumerEmailId)) {
					if (!consumerEmailId.equals(producerOrderEntity.getEmail())) {
						producerOrderEntity.setActualPeopleCount(producerOrderEntity.getActualPeopleCount() + 1);
						consumerOrderEntity = new ConsumerOrderEntity();
						consumerOrderEntity.setProducerOrderEntity(producerOrderEntity);
						consumerOrderEntity.setEmail(consumerEmailId);
					} else {
						ResponseUtil.prepareResponse(commonResponseDTO, "You cannot book your own meal.",
								Constants.FAILURE_STRING, "Producer cannot book his own meal.", false);
					}
				} else {
					ResponseUtil.prepareResponse(commonResponseDTO, "Invalid email id ", Constants.FAILURE_STRING,
							"Invalid email id ", false);
				}
			} else {
				ResponseUtil.prepareResponse(commonResponseDTO, "Can't register, maximum attendee count reached.",
						Constants.FAILURE_STRING, "Maximum people count reached ", false);
			}
		}
		return consumerOrderEntity;
	}

	private boolean checkPeopleCount(ProducerOrderEntity producerOrderEntity) {
		boolean countAcceptable = false;
		if (producerOrderEntity.getActualPeopleCount() < producerOrderEntity.getMaxPeopleCount()) {
			countAcceptable = true;
		}
		return countAcceptable;
	}

	@SuppressWarnings("unchecked")
	@Override
	public DataResponseDTO fetchAllProducerOrders() {
		ApplicationLogger.logInfoMessage("Starting BookingServiceImpl for fetch all producer orders",
				BookingServiceImpl.class);
		DataResponseDTO response = new DataResponseDTO();
		try {
			Integer count = 30;
			List<ProducerOrderEntity> prodEntities = producerOrderRepository
					.findAllProducerRecords(PageRequest.of(0, count));
			if (!prodEntities.isEmpty()) {
				DataResponseDTO dataResponseDTO = getProducersNameAndAddress(prodEntities);
				if (dataResponseDTO.isSuccess()) {
					Map<String, Object> userInfo = (Map<String, Object>) dataResponseDTO.getData().get("userInfo");
					Map<String, List<ProducerOrderResponseDto>> responseMap = getResponseMap(userInfo, prodEntities);
					response.setData(getRelevantData(responseMap.get("producerOrders")));
					ResponseUtil.prepareResponse(response, "Successfully fetched Producer Orders.",
							Constants.SUCCESS_STRING, "Successfully fetched Producer Orders.", true);
				}
			}
		} catch (Exception ex) {
			ResponseUtil.prepareResponse(response, "Failed to fetch producer orders.", Constants.FAILURE_STRING,
					"Exception occurred while trying to fetch producer orders. Exception msg: " + ex.getMessage(),
					false);
		}
		return response;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Map getRelevantData(List<ProducerOrderResponseDto> orders) {
		Map<String, Object> data = new HashMap<>();
		List<ProducerOrdersListingResponseDto> producerOrdersList = new ArrayList<>();
		ProducerOrdersListingResponseDto producerOrder;
		Set<String> itemNames;
		String imageUrl;
		String imageThumbnailUrl;
		String cuisine;
		for (ProducerOrderResponseDto order : orders) {
			imageUrl = "";
			imageThumbnailUrl = "";
			cuisine = "";
			itemNames = new HashSet<>();
			producerOrder = new ProducerOrdersListingResponseDto();
			producerOrder.setOrderId(order.getProducerOrderId());
			producerOrder.setStreetName((String) ((Map<String, Object>) order.getAddress()).get("streetName"));
			Set<ItemEntity> items = order.getItemList();
			for (ItemEntity item : items) {
				itemNames.add(item.getName());
				if ("Main Course".equalsIgnoreCase(item.getType())) {
					imageThumbnailUrl = item.getImageThumbnailUrl();
					imageUrl = item.getImageUrl();
					cuisine = item.getCuisine();
				}
			}
			if ((Util.isStringEmpty(imageUrl) || Util.isStringEmpty(imageThumbnailUrl)) && !items.isEmpty()) {
				imageThumbnailUrl = items.iterator().next().getImageThumbnailUrl();
				imageUrl = items.iterator().next().getImageUrl();
				cuisine = items.iterator().next().getCuisine();
			}
			producerOrder.setItemNames(itemNames);
			producerOrder.setImageThumbnailUrl(imageThumbnailUrl);
			producerOrder.setImageUrl(imageUrl);
			producerOrder.setCuisine(cuisine);
			producerOrder.setTotalAttendees(order.getMaxPeopleCount());
			producerOrder.setCurrentAttendeeCount(order.getActualPeopleCount());
			producerOrdersList.add(producerOrder);
		}
		data.put("producerOrders", producerOrdersList);
		return data;
	}

	private DataResponseDTO getProducersNameAndAddress(List<ProducerOrderEntity> prodEntities) {
		Map<String, Set<String>> emailIdMap = new HashMap<>();
		Set<String> emailIdSet = new HashSet<>();
		for (ProducerOrderEntity producerOrderEntity : prodEntities) {
			if (producerOrderEntity.getEmail() != null) {
				emailIdSet.add(producerOrderEntity.getEmail());
			}
		}
		emailIdMap.put("emailIds", emailIdSet);
		final String uri = userMicroserviceDomain + ":" + userMicroservicePort
				+ "/eatngreet/userms/user/get-users-info";
		HttpHeaders headers = new HttpHeaders();
		headers.set("Content-Type", "application/json");
		HttpEntity<Map<String, Set<String>>> entity = new HttpEntity<>(emailIdMap, headers);
		RestTemplate restTemplate = new RestTemplate();
		return restTemplate.postForObject(uri, entity, DataResponseDTO.class);
	}

	@SuppressWarnings("unchecked")
	private Map<String, List<ProducerOrderResponseDto>> getResponseMap(Map<String, Object> userAddressMap,
			List<ProducerOrderEntity> prodEntities) {
		Map<String, List<ProducerOrderResponseDto>> producerOrderResponseMap = new HashMap<>();
		List<ProducerOrderResponseDto> prodOrderResponseDtoList = new ArrayList<>();
		if (!userAddressMap.isEmpty() && !prodEntities.isEmpty()) {
			for (ProducerOrderEntity prodEntity : prodEntities) {
				if (prodEntity.getEmail() != null) {
					Map<String, Object> nameAddressMap = (Map<String, Object>) userAddressMap
							.get(prodEntity.getEmail());
					ProducerOrderResponseDto producerOrderResponseDto = new ProducerOrderResponseDto();
					producerOrderResponseDto.setProducerOrderId(prodEntity.getProducerOrderId());
					if (!nameAddressMap.isEmpty()) {
						producerOrderResponseDto.setFirstName((String) nameAddressMap.get("firstName"));
						producerOrderResponseDto.setLastName((String) nameAddressMap.get("lastName"));
						producerOrderResponseDto.setAddress(((List<Object>) nameAddressMap.get("addresses")).get(0));
					}
					producerOrderResponseDto.setItemList(prodEntity.getItemList());
					producerOrderResponseDto.setEmail(prodEntity.getEmail());
					producerOrderResponseDto.setImageUrls(getImageUrls(prodEntity.getItemList()));
					producerOrderResponseDto.setImageThumbnailUrls(getThumbnailImageUrls(prodEntity.getItemList()));
					producerOrderResponseDto.setActualPeopleCount(prodEntity.getActualPeopleCount());
					producerOrderResponseDto.setMaxPeopleCount(prodEntity.getMaxPeopleCount());
					producerOrderResponseDto.setNote(prodEntity.getNote());
					producerOrderResponseDto.setOtherItems(prodEntity.getOtherItems());
					producerOrderResponseDto.setPaymentDeadline(prodEntity.getPaymentDeadline());
					producerOrderResponseDto.setPreferenceType(prodEntity.getPreferenceType());
					producerOrderResponseDto.setPrice(prodEntity.getPrice());
					producerOrderResponseDto.setReservationDeadline(prodEntity.getReservationDeadline());
					producerOrderResponseDto.setServingDate(prodEntity.getServingDate());
					prodOrderResponseDtoList.add(producerOrderResponseDto);
				}
			}
		}
		producerOrderResponseMap.put("producerOrders", prodOrderResponseDtoList);
		return producerOrderResponseMap;
	}

	private List<String> getImageUrls(Set<ItemEntity> itemList) {
		List<String> imageUrls = new ArrayList<>();
		for (ItemEntity itemEntity : itemList) {
			if (!Util.isStringEmpty(itemEntity.getImageUrl())) {
				imageUrls.add(itemEntity.getImageUrl());
			}
		}
		return imageUrls;
	}

	private List<String> getThumbnailImageUrls(Set<ItemEntity> itemList) {
		List<String> imageUrls = new ArrayList<>();
		for (ItemEntity itemEntity : itemList) {
			if (!Util.isStringEmpty(itemEntity.getImageThumbnailUrl())) {
				imageUrls.add(itemEntity.getImageThumbnailUrl());
			}
		}
		return imageUrls;
	}

	@SuppressWarnings("unchecked")
	@Override
	public CommonResponseDto fetchSingleItem(Long producerOrderId) {
		ApplicationLogger.logInfoMessage("Starting BookingServiceImpl for fetching single producer order",
				BookingServiceImpl.class);
		DataResponseDTO response = new DataResponseDTO();
		try {
			if (!Util.isLongValueEmpty(producerOrderId)) {
				List<ProducerOrderEntity> prodEntityList = producerOrderRepository
						.findByProducerOrderId(producerOrderId);
				if (!prodEntityList.isEmpty()) {
					ProducerOrderEntity prodEntity = prodEntityList.get(0);
					if (prodEntity != null) {
						List<ProducerOrderEntity> prodEntities = new ArrayList<>();
						prodEntities.add(prodEntity);
						DataResponseDTO dataResponseDTO = getProducersNameAndAddress(prodEntities);
						if (dataResponseDTO.isSuccess()) {
							Map<String, Object> userResponse = dataResponseDTO.getData();
							Map<String, List<ProducerOrderResponseDto>> responseMap = getResponseMap(
									(Map<String, Object>) userResponse.get("userInfo"), prodEntities);
							response.setData(responseMap);
							ResponseUtil.prepareResponse(response, "Successfully fetched order.",
									Constants.SUCCESS_STRING, "Successfully fetched order.", true);
						} else {
							ResponseUtil.prepareResponse(response, "Please try again later.", Constants.FAILURE_STRING,
									"Some problem occurred in fetching producers name and address. Info: "
											+ dataResponseDTO.getInfo(),
									false);
						}
					}
				} else {
					ResponseUtil.prepareResponse(response, "Meal not found. Please try again later.",
							Constants.FAILURE_STRING, "No order found corresponding to requested orderId.", false);
				}
			}
		} catch (Exception ex) {
			ResponseUtil.prepareResponse(response, "Some problem occurred. Please try again.", Constants.FAILURE_STRING,
					"Exception occurred while trying to fetch single producer order. Exception message: "
							+ ex.getMessage(),
					false);
		}
		return response;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public DataResponseDTO fetchSingleConsumerItem(HttpServletRequest request) {
		DataResponseDTO response = new DataResponseDTO();
		Map<String, List<MyConsumerOrdersResponseDto>> consumerOrderResponseMap = new HashMap<>();
		String email = Util.getDecryptedEmail(request);
		if (Util.isValidEmail(email)) {
			List<ConsumerOrderEntity> consumerOrderEntityList = consumerOrderRepository.findByEmail(email);
			if (!consumerOrderEntityList.isEmpty()) {
				Map<String, Set<String>> emailIdMap = new HashMap<>();
				Set<String> emailIdSet = new HashSet<>();
				for (ConsumerOrderEntity consumerOrder : consumerOrderEntityList) {
					if (consumerOrder.getEmail() != null) {
						emailIdSet.add(consumerOrder.getProducerOrderEntity().getEmail());
					}
				}
				emailIdMap.put("emailIds", emailIdSet);
				final String uri = userMicroserviceDomain + ":" + userMicroservicePort
						+ "/eatngreet/userms/user/get-users-info";
				HttpHeaders headers = new HttpHeaders();
				headers.set("Content-Type", "application/json");
				HttpEntity<Map<String, Set<String>>> entity = new HttpEntity<>(emailIdMap, headers);
				RestTemplate restTemplate = new RestTemplate();
				DataResponseDTO map = restTemplate.postForObject(uri, entity, DataResponseDTO.class);
				Map<String, Object> userNameAddressMap = (Map<String, Object>) map.getData().get("userInfo");
				List<MyConsumerOrdersResponseDto> consumerOrders = new ArrayList<>();
				for (ConsumerOrderEntity consumerOrderEntity : consumerOrderEntityList) {
					MyConsumerOrdersResponseDto consumerOrderResponseObj = new MyConsumerOrdersResponseDto();
					if (consumerOrderEntity.getProducerOrderEntity() != null) {
						Map<String, Object> userNameAddressInfo = (Map<String, Object>) userNameAddressMap
								.get(consumerOrderEntity.getProducerOrderEntity().getEmail());
						consumerOrderResponseObj
								.setItemList(consumerOrderEntity.getProducerOrderEntity().getItemList());
						consumerOrderResponseObj
								.setServingDate(consumerOrderEntity.getProducerOrderEntity().getServingDate());
						consumerOrderResponseObj.setPrice(consumerOrderEntity.getProducerOrderEntity().getPrice());
						consumerOrderResponseObj
								.setPreference(consumerOrderEntity.getProducerOrderEntity().getPreferenceType());
						consumerOrderResponseObj.setFirstName((String) userNameAddressInfo.get("firstName"));
						consumerOrderResponseObj.setLastName((String) userNameAddressInfo.get("lastName"));
						consumerOrderResponseObj.setAddress(((List) userNameAddressInfo.get("addresses")).get(0));
						consumerOrders.add(consumerOrderResponseObj);
					}
				}
				ResponseUtil.prepareResponse(response, "Successfully fetched orders.", Constants.SUCCESS_STRING,
						"Successfully fetched orders.", true);
				consumerOrderResponseMap.put("consumerOrders", consumerOrders);
				response.setData(consumerOrderResponseMap);
			} else {
				ResponseUtil.prepareResponse(response, "No orders exist.", Constants.SUCCESS_STRING, "No orders exist.",
						true);
			}
		} else {
			ResponseUtil.prepareResponse(response, "Please try again later.", Constants.FAILURE_STRING,
					"Invalid email id decrypted from header auth token. Decrypted email id: " + email, false);
		}

		return response;
	}

	@Override
	public CommonResponseDto fetchSingleProducerItem(HttpServletRequest request) {
		DataResponseDTO response = new DataResponseDTO();
		Map<String, List<MyProducerOrdersResponseDto>> prodMap = new HashMap<>();
		List<MyProducerOrdersResponseDto> producerOrderResponseDtoList = new ArrayList<>();
		try {
			String email = Util.getDecryptedEmail(request);
			if (Util.isValidEmail(email)) {
				List<ProducerOrderEntity> producerOrderEntityList = producerOrderRepository.findByEmail(email);
				if (!producerOrderEntityList.isEmpty()) {
					for (ProducerOrderEntity producerOrderEntity : producerOrderEntityList) {
						MyProducerOrdersResponseDto producerOrderResponseDto = new MyProducerOrdersResponseDto();
						producerOrderResponseDto.setPrice(producerOrderEntity.getPrice());
						producerOrderResponseDto.setPreference(producerOrderEntity.getPreferenceType());
						producerOrderResponseDto.setItemList(producerOrderEntity.getItemList());
						producerOrderResponseDto.setServingDate(producerOrderEntity.getServingDate());
						producerOrderResponseDto.setActualPeopleCount(producerOrderEntity.getActualPeopleCount());
						producerOrderResponseDto.setMaxPeopleCount(producerOrderEntity.getMaxPeopleCount());
						producerOrderResponseDto.setPaymentDeadline(producerOrderEntity.getPaymentDeadline());
						producerOrderResponseDto.setReservationDeadline(producerOrderEntity.getReservationDeadline());
						producerOrderResponseDto.setOtherItems(producerOrderEntity.getOtherItems());
						producerOrderResponseDto.setNote(producerOrderEntity.getNote());
						producerOrderResponseDtoList.add(producerOrderResponseDto);
					}
					prodMap.put("producerOrders", producerOrderResponseDtoList);
					ResponseUtil.prepareResponse(response, "Successfully fetched producer orders.",
							Constants.SUCCESS_STRING, "Successfully fetched producer orders.", true);
				} else {
					ResponseUtil.prepareResponse(response, "No orders exist.", Constants.FAILURE_STRING,
							"No orders exist.", false);
				}

			} else {
				ResponseUtil.prepareResponse(response, "Please try again later.", Constants.FAILURE_STRING,
						"Invalid email id decrypted from headers.", false);
			}
		} catch (Exception e) {
			ResponseUtil.prepareResponse(response, "Some problem occurred. Please try again later.",
					Constants.FAILURE_STRING,
					"Exception occurred in fetching producer orders. Exception message: " + e.getMessage(), false);
		}

		response.setData(prodMap);
		return response;
	}
}
