package com.nus.iss.eatngreet.booking.responsedto;

import java.util.Date;
import java.util.Set;

import com.nus.iss.eatngreet.booking.entity.ItemEntity;

import lombok.Data;

@Data
public class MyProducerOrdersResponseDto {
	private Set<ItemEntity> itemList;
	private Date servingDate;
	private Date paymentDeadline;
	private Date reservationDeadline;
	private Float price;
	private Long maxPeopleCount;
	private Long actualPeopleCount = 0L;
	private Long preference;
	private String otherItems;
	private String note;
}
