package com.nus.iss.eatngreet.booking.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.nus.iss.eatngreet.booking.entity.ConsumerOrderEntity;
import com.nus.iss.eatngreet.booking.entity.ProducerOrderEntity;
import com.nus.iss.eatngreet.booking.entity.ItemEntity;
import com.nus.iss.eatngreet.booking.repository.ConsumerOrderRepository;
import com.nus.iss.eatngreet.booking.repository.ItemRepository;
import com.nus.iss.eatngreet.booking.repository.ProducerOrderRepository;
import com.nus.iss.eatngreet.booking.requestdto.CreateMealRequestDto;
import com.nus.iss.eatngreet.booking.requestdto.GuestJoiningRequestDto;
import com.nus.iss.eatngreet.booking.responsedto.CommonResponseDto;
import com.nus.iss.eatngreet.booking.responsedto.DataResponseDto;
import com.nus.iss.eatngreet.booking.responsedto.HostOrderResponseDto;
import com.nus.iss.eatngreet.booking.responsedto.MyConsumerOrdersResponseDto;
import com.nus.iss.eatngreet.booking.responsedto.MyProducerOrdersResponseDto;
import com.nus.iss.eatngreet.booking.responsedto.ProducerOrdersListingResponseDto;
import com.nus.iss.eatngreet.booking.service.BookingService;
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

	private static final Logger logger = LoggerFactory.getLogger(BookingServiceImpl.class);

	@SuppressWarnings("unchecked")
	@Override
	public CommonResponseDto createMeal(HttpServletRequest request, CreateMealRequestDto meal) {
		CommonResponseDto response = new CommonResponseDto();
		try {
			logger.info("In createMeal() of BookingServiceImpl with request: {}", meal);
			if (checkMealObj(meal, response)) {
				ProducerOrderEntity producerOrderEntity = createProducerOrderEntity(Util.getDecryptedEmail(request), meal);
				producerOrderRepository.save(producerOrderEntity);
				ResponseUtil.prepareResponse(response, Constants.MEAL_POSTING_SUCCESS_MSG, Constants.SUCCESS_STRING,
						Constants.MEAL_POSTING_SUCCESS_MSG, true);
				List<ProducerOrderEntity> producerOrders = new ArrayList<>();
				producerOrders.add(producerOrderEntity);
				DataResponseDto userDetailsResponse = getHostNameAndAddress(producerOrders);
				if (userDetailsResponse.isSuccess()) {
					logger.info("Successfully fetched user details required for sending the notification mail.");
					Map<String, Object> userResponse = userDetailsResponse.getData();
					Map<String, Object> info = (Map<String, Object>) userResponse.get(Constants.USER_INFO_KEY);
					Map<String, Object> producerDetails = (Map<String, Object>) info
							.get(producerOrderEntity.getEmail());
					String name = producerDetails.get(Constants.FIRST_NAME_KEY) + " " + producerDetails.get(Constants.LAST_NAME_KEY);
					CommonResponseDto notificationResponse = sendConfirmationEmail(producerOrderEntity, name);
					if (notificationResponse.isSuccess()) {
						logger.info("Notification mail sent successfully.");
						ResponseUtil.prepareResponse(response, Constants.MEAL_POSTING_SUCCESS_MSG,
								Constants.SUCCESS_STRING,
								"Successfully created meal and successfully sent the required mail.", true);
					} else {
						logger.info("Notification mail was not sent. Reason: {}", notificationResponse.getInfo());
						ResponseUtil.prepareResponse(response, Constants.MEAL_POSTING_SUCCESS_MSG,
								Constants.SUCCESS_STRING,
								"Successfully created meal but notification mail was not sent. Reason:"
										+ notificationResponse.getInfo(),
								true);
					}
				} else {
					logger.error(
							"UnablE to fetch user details that are required for sending the notification mail. Reason: {}",
							userDetailsResponse.getInfo());
					ResponseUtil.prepareResponse(response, Constants.MEAL_POSTING_SUCCESS_MSG, Constants.SUCCESS_STRING,
							"Some problem occurred in fetching producers name and address for sending the email, hence email for this was not sent. Info: "
									+ userDetailsResponse.getInfo(),
							true);
				}
			} else {
				logger.info("Validations failed for request object: {}", meal);
			}
		} catch (Exception ex) {
			logger.error("Exception occurred while trying to create new meal. Exception message: {}", ex.getMessage());
			ResponseUtil.prepareResponse(response, Constants.TRY_AGAIN_MSG_FOR_USERS, Constants.FAILURE_STRING,
					"Following exception occurred while trying to create producer order: " + ex.getMessage(), false);
		}
		return response;
	}

	private CommonResponseDto sendConfirmationEmail(ProducerOrderEntity order, String name) {
		logger.info("sendConfirmationEmail() of BookingServiceImpl. Request: {}, name: {}", order, name);
		Map<String, Object> notificationReqMap = new HashMap<>();
		notificationReqMap.put("name", name);
		notificationReqMap.put("servingDate", order.getServingDate());
		notificationReqMap.put("count", order.getMaxPeopleCount());
		notificationReqMap.put("email", order.getEmail());
		final String uri = notificationMicroserviceDomain + ":" + notificationMicroservicePort
				+ Constants.NEW_MEAL_NOTIFICATION_API_URL;
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set(Constants.AUTHORIZATION_HEADER_NAME, "Bearer " + emailAuthToken);
		HttpEntity<Map<String, Object>> entity = new HttpEntity<>(notificationReqMap, headers);
		RestTemplate restTemplate = new RestTemplate();
		return restTemplate.postForObject(uri, entity, CommonResponseDto.class);
	}

	private ProducerOrderEntity createProducerOrderEntity(String consumerEmailId, CreateMealRequestDto producerOrder) {
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

	private Set<ItemEntity> getItems(CreateMealRequestDto producerOrder) {
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

	private boolean checkMealObj(CreateMealRequestDto producerOrder, CommonResponseDto commonResponseDTO) {
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
	public CommonResponseDto joinMeal(HttpServletRequest request, GuestJoiningRequestDto guestOrder) {
		logger.info("joinMeal() of BookingServiceImpl. Request: {}.", guestOrder);
		CommonResponseDto response = new CommonResponseDto();
		try {
			if (!Util.isLongValueEmpty(guestOrder.getProducerOrderId())) {
				String guestEmailId = Util.getDecryptedEmail(request);
				logger.info("Decrypted guest email id: {}.", guestEmailId);
				ConsumerOrderEntity guestOrderEntity = createGuestOrderEntity(guestEmailId, guestOrder, response);
				if (guestOrderEntity != null) {
					Float amount = guestOrderEntity.getProducerOrderEntity().getPrice();
					Map<String, Object> txnReqMap = new HashMap<>();
					txnReqMap.put("consumerEmailId", guestEmailId);
					txnReqMap.put("producerEmailId", guestOrderEntity.getProducerOrderEntity().getEmail());
					txnReqMap.put("producerOrderId", guestOrderEntity.getProducerOrderEntity().getProducerOrderId());
					txnReqMap.put("amount", amount);
					final String uri = paymentMicroserviceDomain + ":" + paymentMicroservicePort
							+ Constants.PAY_FOR_MEAL_API_URL;
					HttpHeaders headers = new HttpHeaders();
					headers.setContentType(MediaType.APPLICATION_JSON);
					HttpEntity<Map<String, Object>> entity = new HttpEntity<>(txnReqMap, headers);
					RestTemplate restTemplate = new RestTemplate();
					CommonResponseDto paymentResponse = restTemplate.postForObject(uri, entity,
							CommonResponseDto.class);
					if (paymentResponse != null && paymentResponse.isSuccess()) {
						logger.info("Successfully processed payment.");
						consumerOrderRepository.save(guestOrderEntity);
						ResponseUtil.prepareResponse(response, "Successfully joined the meal.",
								Constants.SUCCESS_STRING, "Customer has successfully joined the meal.", true);
						CommonResponseDto notificationResponse = sendJoiningEmail(guestOrderEntity);
						if (notificationResponse.isSuccess()) {
							logger.info("Successfully sent notification mail.");
							ResponseUtil.prepareResponse(response, "Successfully joined meal.",
									Constants.SUCCESS_STRING,
									"Successfully joined meal and successfully sent the required mail.", true);
						} else {
							logger.error("Notification mail couldn't be sent. Response msg: {}.",
									notificationResponse.getInfo());
							ResponseUtil.prepareResponse(response, "Successfully joined meal.",
									Constants.SUCCESS_STRING,
									"Successfully joined meal but notification mail was not sent. Reason:"
											+ notificationResponse.getInfo(),
									true);
						}
					} else if (paymentResponse == null) {
						logger.error("Null response received from pay/now api.");
						ResponseUtil.prepareResponse(response, "Couldn't process payment. Please try again later.",
								Constants.FAILURE_STRING, "Couldn't process payment. Null returned from pay now api.",
								false);
					} else {
						logger.error("Payment couldn't be processed. Response msg: {}.", paymentResponse.getInfo());
						ResponseUtil.prepareResponse(response,
								"Couldn't process payment. " + paymentResponse.getMessage(), Constants.FAILURE_STRING,
								"Couldn't process payment (as per the response from Payment Microservice). Response: "
										+ paymentResponse.getMessage(),
								false);
					}
				}
			} else {
				logger.error("Meal ID is missing in the request.");
				ResponseUtil.prepareResponse(response, "Meal ID missing.", Constants.FAILURE_STRING,
						"Meal ID not present in the request.", false);
			}
		} catch (Exception ex) {
			logger.error("Exception occurred while trying to add guest to a meal. Exception message: {}.",
					ex.getMessage());
			ResponseUtil.prepareResponse(response, Constants.TRY_AGAIN_MSG_FOR_USERS, Constants.FAILURE_STRING,
					"Following exception occurred " + ex.getMessage(), false);
		}
		return response;
	}

	@SuppressWarnings("unchecked")
	private CommonResponseDto sendJoiningEmail(ConsumerOrderEntity order) {
		logger.info("sendJoiningEmail() of BookingServiceImpl. Request obj: {}", order);
		CommonResponseDto response = new CommonResponseDto();
		Map<String, Object> notificationReqMap = new HashMap<>();
		Set<String> emailIds = new HashSet<>();
		emailIds.add(order.getEmail());
		emailIds.add(order.getProducerOrderEntity().getEmail());
		Map<String, Set<String>> emailIdMap = new HashMap<>();
		emailIdMap.put(Constants.EMAIL_IDS_KEY, emailIds);
		final String uri = userMicroserviceDomain + ":" + userMicroservicePort + Constants.GET_USERS_INFO_API_URL;
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<Map<String, Set<String>>> userInfoReqEntity = new HttpEntity<>(emailIdMap, headers);
		RestTemplate restTemplate = new RestTemplate();
		DataResponseDto userResponse = restTemplate.postForObject(uri, userInfoReqEntity, DataResponseDto.class);
		if (userResponse.isSuccess()) {
			Map<String, Object> details = (Map<String, Object>) userResponse.getData().get(Constants.USER_INFO_KEY);
			Map<String, Object> hostDetails = (Map<String, Object>) details
					.get(order.getProducerOrderEntity().getEmail());
			Map<String, Object> guestDetails = (Map<String, Object>) details.get(order.getEmail());
			notificationReqMap.put("hostName", hostDetails.get(Constants.FIRST_NAME_KEY) + " " + hostDetails.get(Constants.LAST_NAME_KEY));
			notificationReqMap.put("guestName", guestDetails.get(Constants.FIRST_NAME_KEY) + " " + guestDetails.get(Constants.LAST_NAME_KEY));
			notificationReqMap.put("hostEmailId", order.getProducerOrderEntity().getEmail());
			notificationReqMap.put("guestEmailId", order.getEmail());
			notificationReqMap.put("servingDate", order.getProducerOrderEntity().getServingDate());
			notificationReqMap.put("maxCount", order.getProducerOrderEntity().getMaxPeopleCount());
			notificationReqMap.put("guestCount", order.getProducerOrderEntity().getActualPeopleCount());
			final String notificationUri = notificationMicroserviceDomain + ":" + notificationMicroservicePort
					+ "/eatngreet/notificationms/notify/confirm-joining-meal";
			headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.set(Constants.AUTHORIZATION_HEADER_NAME, "Bearer " + emailAuthToken);
			HttpEntity<Map<String, Object>> entity = new HttpEntity<>(notificationReqMap, headers);
			restTemplate = new RestTemplate();
			CommonResponseDto notificationResponse = restTemplate.postForObject(notificationUri, entity,
					CommonResponseDto.class);
			if (notificationResponse.isSuccess()) {
				logger.info("Notification mail sent sucessfully.");
				ResponseUtil.prepareResponse(response, "Mail sent successfully.", Constants.SUCCESS_STRING,
						"Mail sent successfully.", true);
			} else {
				logger.error("Mail was not sent. Reason: {}.", notificationResponse.getInfo());
				ResponseUtil.prepareResponse(response, "Mail was not sent.", Constants.FAILURE_STRING,
						"Mail was not sent. Reason: " + notificationResponse.getInfo(), false);
			}
		} else {
			logger.error("Unable to fetch user info. Reason: {}.", userResponse.getInfo());
			ResponseUtil.prepareResponse(response, "Unable to fetch user info.", Constants.FAILURE_STRING,
					"Unable to fetch user info. Reason: " + userResponse.getInfo(), false);
		}
		return response;

	}

	private ConsumerOrderEntity createGuestOrderEntity(String guestEmailId, GuestJoiningRequestDto guestOrder,
			CommonResponseDto commonResponseDTO) {
		logger.info("createGuestOrderEntity() of BookingServiceImpl.");
		ConsumerOrderEntity guestOrderEntity = null;
		List<ProducerOrderEntity> hostOrderEntityList = producerOrderRepository
				.findByProducerOrderId(guestOrder.getProducerOrderId());
		if (!hostOrderEntityList.isEmpty()) {
			ProducerOrderEntity hostOrderEntity = hostOrderEntityList.get(0);
			if (checkGuestCountInAMeal(hostOrderEntity)) {
				if (Util.isValidEmail(guestEmailId)) {
					if (!guestEmailId.equals(hostOrderEntity.getEmail())) {
						hostOrderEntity.setActualPeopleCount(hostOrderEntity.getActualPeopleCount() + 1);
						guestOrderEntity = new ConsumerOrderEntity();
						guestOrderEntity.setProducerOrderEntity(hostOrderEntity);
						guestOrderEntity.setEmail(guestEmailId);
					} else {
						logger.info("Host trying to book own meal.");
						ResponseUtil.prepareResponse(commonResponseDTO, "You cannot book your own meal.",
								Constants.FAILURE_STRING, "Producer cannot book his own meal.", false);
					}
				} else {
					logger.error("Invalid email id decrypted from headers. Decrypted email id: {}.", guestEmailId);
					ResponseUtil.prepareResponse(commonResponseDTO, "Invalid email id.", Constants.FAILURE_STRING,
							"Invalid email id ", false);
				}
			} else {
				logger.error("Host's meal has already been completely booked.");
				ResponseUtil.prepareResponse(commonResponseDTO, "Can't register, maximum guest count reached.",
						Constants.FAILURE_STRING, "Maximum people count reached ", false);
			}
		}
		return guestOrderEntity;
	}

	private boolean checkGuestCountInAMeal(ProducerOrderEntity producerOrderEntity) {
		boolean countAcceptable = false;
		if (producerOrderEntity.getActualPeopleCount() < producerOrderEntity.getMaxPeopleCount()) {
			countAcceptable = true;
		}
		return countAcceptable;
	}

	@SuppressWarnings("unchecked")
	@Override
	public DataResponseDto fetchAllActiveMeals() {
		logger.info("fetchAllActiveMeals() of BookingServiceImpl.");
		DataResponseDto response = new DataResponseDto();
		try {
			Integer count = 30;
			List<ProducerOrderEntity> prodEntities = producerOrderRepository
					.findAllProducerRecords(PageRequest.of(0, count));
			if (!prodEntities.isEmpty()) {
				DataResponseDto dataResponseDTO = getHostNameAndAddress(prodEntities);
				if (dataResponseDTO.isSuccess()) {
					Map<String, Object> userInfo = (Map<String, Object>) dataResponseDTO.getData().get(Constants.USER_INFO_KEY);
					Map<String, List<HostOrderResponseDto>> responseMap = getResponseMap(userInfo, prodEntities);
					response.setData(getRelevantData(responseMap.get(Constants.HOST_ORDERS_KEY)));
					ResponseUtil.prepareResponse(response, "Successfully fetched Producer Orders.",
							Constants.SUCCESS_STRING, "Successfully fetched Producer Orders.", true);
				}
			} else {
				logger.warn("No active meals exist on the platform.");
			}
		} catch (Exception ex) {
			logger.error("Exception occurred while trying to fetch all active meals. Exception message: {}",
					ex.getMessage());
			ResponseUtil.prepareResponse(response, "Failed to fetch producer orders.", Constants.FAILURE_STRING,
					"Exception occurred while trying to fetch producer orders. Exception msg: " + ex.getMessage(),
					false);
		}
		return response;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Map getRelevantData(List<HostOrderResponseDto> hostOrders) {
		logger.info("getRelevantData() of BookingServiceImpl.");
		Map<String, Object> data = new HashMap<>();
		List<ProducerOrdersListingResponseDto> producerOrdersList = new ArrayList<>();
		ProducerOrdersListingResponseDto producerOrder;
		Set<String> itemNames;
		String imageUrl;
		String imageThumbnailUrl;
		String cuisine;
		for (HostOrderResponseDto hostOrder : hostOrders) {
			imageUrl = "";
			imageThumbnailUrl = "";
			cuisine = "";
			itemNames = new HashSet<>();
			producerOrder = new ProducerOrdersListingResponseDto();
			producerOrder.setOrderId(hostOrder.getProducerOrderId());
			producerOrder.setStreetName((String) ((Map<String, Object>) hostOrder.getAddress()).get("streetName"));
			Set<ItemEntity> items = hostOrder.getItemList();
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
			producerOrder.setTotalAttendees(hostOrder.getMaxPeopleCount());
			producerOrder.setCurrentAttendeeCount(hostOrder.getActualPeopleCount());
			producerOrdersList.add(producerOrder);
		}
		data.put(Constants.HOST_ORDERS_KEY, producerOrdersList);
		return data;
	}

	private DataResponseDto getHostNameAndAddress(List<ProducerOrderEntity> hostEntities) {
		logger.info("getHostNameAndAddress of BookingServiceImpl.");
		Map<String, Set<String>> emailIdMap = new HashMap<>();
		Set<String> emailIdSet = new HashSet<>();
		for (ProducerOrderEntity hostEntity : hostEntities) {
			if (hostEntity.getEmail() != null) {
				emailIdSet.add(hostEntity.getEmail());
			}
		}
		emailIdMap.put(Constants.EMAIL_IDS_KEY, emailIdSet);
		final String uri = userMicroserviceDomain + ":" + userMicroservicePort + Constants.GET_USERS_INFO_API_URL;
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<Map<String, Set<String>>> entity = new HttpEntity<>(emailIdMap, headers);
		RestTemplate restTemplate = new RestTemplate();
		return restTemplate.postForObject(uri, entity, DataResponseDto.class);
	}

	@SuppressWarnings("unchecked")
	private Map<String, List<HostOrderResponseDto>> getResponseMap(Map<String, Object> userAddressMap,
			List<ProducerOrderEntity> hostEntities) {
		logger.info("getResponseMap() of BookingServiceImpl.");
		Map<String, List<HostOrderResponseDto>> producerOrderResponseMap = new HashMap<>();
		List<HostOrderResponseDto> prodOrderResponseDtoList = new ArrayList<>();
		if (!userAddressMap.isEmpty() && !hostEntities.isEmpty()) {
			for (ProducerOrderEntity hostEntity : hostEntities) {
				if (hostEntity.getEmail() != null) {
					Map<String, Object> nameAddressMap = (Map<String, Object>) userAddressMap
							.get(hostEntity.getEmail());
					HostOrderResponseDto producerOrderResponseDto = new HostOrderResponseDto();
					producerOrderResponseDto.setProducerOrderId(hostEntity.getProducerOrderId());
					if (!nameAddressMap.isEmpty()) {
						producerOrderResponseDto.setFirstName((String) nameAddressMap.get(Constants.FIRST_NAME_KEY));
						producerOrderResponseDto.setLastName((String) nameAddressMap.get(Constants.LAST_NAME_KEY));
						producerOrderResponseDto.setAddress(((List<Object>) nameAddressMap.get("addresses")).get(0));
					}
					producerOrderResponseDto.setItemList(hostEntity.getItemList());
					producerOrderResponseDto.setEmail(hostEntity.getEmail());
					producerOrderResponseDto.setImageUrls(getImageUrls(hostEntity.getItemList()));
					producerOrderResponseDto.setImageThumbnailUrls(getThumbnailUrls(hostEntity.getItemList()));
					producerOrderResponseDto.setActualPeopleCount(hostEntity.getActualPeopleCount());
					producerOrderResponseDto.setMaxPeopleCount(hostEntity.getMaxPeopleCount());
					producerOrderResponseDto.setNote(hostEntity.getNote());
					producerOrderResponseDto.setOtherItems(hostEntity.getOtherItems());
					producerOrderResponseDto.setPaymentDeadline(hostEntity.getPaymentDeadline());
					producerOrderResponseDto.setPreferenceType(hostEntity.getPreferenceType());
					producerOrderResponseDto.setPrice(hostEntity.getPrice());
					producerOrderResponseDto.setReservationDeadline(hostEntity.getReservationDeadline());
					producerOrderResponseDto.setServingDate(hostEntity.getServingDate());
					prodOrderResponseDtoList.add(producerOrderResponseDto);
				}
			}
		}
		producerOrderResponseMap.put(Constants.HOST_ORDERS_KEY, prodOrderResponseDtoList);
		return producerOrderResponseMap;
	}

	private List<String> getImageUrls(Set<ItemEntity> itemList) {
		logger.info("getImageUrls() of BookingServiceImpl.");
		List<String> imageUrls = new ArrayList<>();
		for (ItemEntity itemEntity : itemList) {
			if (!Util.isStringEmpty(itemEntity.getImageUrl())) {
				imageUrls.add(itemEntity.getImageUrl());
			}
		}
		return imageUrls;
	}

	private List<String> getThumbnailUrls(Set<ItemEntity> itemList) {
		logger.info("getThumbnailUrls() of BookingServiceImpl.");
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
	public DataResponseDto fetchSingleMeal(Long mealId) {
		logger.info("fetchSingleMeal() of BookingServiceImpl.");
		DataResponseDto response = new DataResponseDto();
		try {
			if (!Util.isLongValueEmpty(mealId)) {
				List<ProducerOrderEntity> prodEntityList = producerOrderRepository.findByProducerOrderId(mealId);
				if (!prodEntityList.isEmpty()) {
					ProducerOrderEntity prodEntity = prodEntityList.get(0);
					if (prodEntity != null) {
						List<ProducerOrderEntity> prodEntities = new ArrayList<>();
						prodEntities.add(prodEntity);
						DataResponseDto dataResponseDTO = getHostNameAndAddress(prodEntities);
						if (dataResponseDTO.isSuccess()) {
							Map<String, Object> userResponse = dataResponseDTO.getData();
							Map<String, List<HostOrderResponseDto>> responseMap = getResponseMap(
									(Map<String, Object>) userResponse.get(Constants.USER_INFO_KEY), prodEntities);
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
					logger.error("No meal found corresponsding to given meal id. Meal id is: {}.", mealId);
					ResponseUtil.prepareResponse(response, "Meal not found. Please try again later.",
							Constants.FAILURE_STRING, "No order found corresponding to requested orderId.", false);
				}
			} else {
				logger.error("Invalid meal ID received in request. Meal id is: {}.", mealId);
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
	public DataResponseDto fetchAllJoinedMealsOfUser(HttpServletRequest request) {
		logger.info("fetchAllJoinedMealsOfUser() of BookingServiceImpl.");
		DataResponseDto response = new DataResponseDto();
		Map<String, List<MyConsumerOrdersResponseDto>> consumerOrderResponseMap = new HashMap<>();
		String email = Util.getDecryptedEmail(request);
		try {
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
					emailIdMap.put(Constants.EMAIL_IDS_KEY, emailIdSet);
					final String uri = userMicroserviceDomain + ":" + userMicroservicePort
							+ Constants.GET_USERS_INFO_API_URL;
					HttpHeaders headers = new HttpHeaders();
					headers.setContentType(MediaType.APPLICATION_JSON);
					HttpEntity<Map<String, Set<String>>> entity = new HttpEntity<>(emailIdMap, headers);
					RestTemplate restTemplate = new RestTemplate();
					DataResponseDto map = restTemplate.postForObject(uri, entity, DataResponseDto.class);
					Map<String, Object> userNameAddressMap = (Map<String, Object>) map.getData().get(Constants.USER_INFO_KEY);
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
							consumerOrderResponseObj.setFirstName((String) userNameAddressInfo.get(Constants.FIRST_NAME_KEY));
							consumerOrderResponseObj.setLastName((String) userNameAddressInfo.get(Constants.LAST_NAME_KEY));
							consumerOrderResponseObj.setAddress(((List) userNameAddressInfo.get("addresses")).get(0));
							consumerOrders.add(consumerOrderResponseObj);
						}
					}
					ResponseUtil.prepareResponse(response, "Successfully fetched orders.", Constants.SUCCESS_STRING,
							"Successfully fetched orders.", true);
					consumerOrderResponseMap.put("consumerOrders", consumerOrders);
					response.setData(consumerOrderResponseMap);
				} else {
					logger.info("User hasn't joined any meals.");
					ResponseUtil.prepareResponse(response, Constants.NO_ORDERS_EXIST, Constants.SUCCESS_STRING,
							Constants.NO_ORDERS_EXIST, true);
				}
			} else {
				logger.error("Invalid email id decrypted from headers. Decrypted email id: {}", email);
				ResponseUtil.prepareResponse(response, "Please try again later.", Constants.FAILURE_STRING,
						"Invalid email id decrypted from header auth token. Decrypted email id: " + email, false);
			}
		} catch (Exception e) {
			logger.error("Exception occurred in fetching joined orders for user: {}. Exception message: {}", email,
					e.getMessage());
			ResponseUtil.prepareResponse(response, "Some problem occurred. Please try again later.",
					Constants.FAILURE_STRING,
					"Exception occurred in fetching joined orders. Exception message: " + e.getMessage(), false);
		}
		return response;
	}

	@Override
	public DataResponseDto fetchAllHostedMealsOfUser(HttpServletRequest request) {
		logger.info("fetchAllJoinedMealsOfUser() of BookingServiceImpl.");
		DataResponseDto response = new DataResponseDto();
		Map<String, List<MyProducerOrdersResponseDto>> prodMap = new HashMap<>();
		List<MyProducerOrdersResponseDto> producerOrderResponseDtoList = new ArrayList<>();
		String email = Util.getDecryptedEmail(request);
		try {
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
					prodMap.put(Constants.HOST_ORDERS_KEY, producerOrderResponseDtoList);
					ResponseUtil.prepareResponse(response, "Successfully fetched producer orders.",
							Constants.SUCCESS_STRING, "Successfully fetched producer orders.", true);
				} else {
					ResponseUtil.prepareResponse(response, Constants.NO_ORDERS_EXIST, Constants.FAILURE_STRING,
							Constants.NO_ORDERS_EXIST, false);
				}

			} else {
				logger.error("Invalid email id decrypted from headers. Decrypted email id: {}", email);
				ResponseUtil.prepareResponse(response, Constants.TRY_AGAIN_MSG_FOR_USERS, Constants.FAILURE_STRING,
						"Invalid email id decrypted from headers. Decrypted email id: " + email, false);
			}
		} catch (Exception e) {
			logger.error("Exception occurred in fetching hosted orders for user: {}. Exception message: {}", email,
					e.getMessage());
			ResponseUtil.prepareResponse(response, Constants.TRY_AGAIN_MSG_FOR_USERS, Constants.FAILURE_STRING,
					"Exception occurred in fetching hosted orders. Exception message: " + e.getMessage(), false);
		}

		response.setData(prodMap);
		return response;
	}
}
