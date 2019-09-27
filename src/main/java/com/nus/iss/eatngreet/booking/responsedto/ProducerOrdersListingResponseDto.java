package com.nus.iss.eatngreet.booking.responsedto;

import java.util.Set;

import lombok.Data;

@Data
public class ProducerOrdersListingResponseDto {

	private Long orderId;
	private String streetName;
	private String imageUrl;
	private String imageThumbnailUrl;
	private Set<String> itemNames;
	private String cuisine;
	private Long totalAttendees;
	private Long currentAttendeeCount;
}
