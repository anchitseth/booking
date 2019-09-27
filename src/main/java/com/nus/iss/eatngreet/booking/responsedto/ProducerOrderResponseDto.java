package com.nus.iss.eatngreet.booking.responsedto;

import java.util.Date;
import java.util.List;
import java.util.Set;

import com.nus.iss.eatngreet.booking.entity.ItemEntity;

import lombok.Data;

@Data
public class ProducerOrderResponseDto {
	private Long producerOrderId;
	private String firstName;
	private String lastName;
	private String email;
	private Set<ItemEntity> itemList;
	private Object address;
	private List<String> imageUrls;
	private List<String> imageThumbnailUrls;
	private Date servingDate;
	private Date paymentDeadline;
	private Date reservationDeadline;
	private Long price;
	private Long maxPeopleCount;
	private Long actualPeopleCount = 0L;
	private Long preferenceType;
	private String otherItems;
	private String note;
}
