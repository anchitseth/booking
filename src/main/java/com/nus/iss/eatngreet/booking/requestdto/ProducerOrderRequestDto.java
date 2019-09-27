package com.nus.iss.eatngreet.booking.requestdto;

import java.util.Date;
import java.util.List;

import lombok.Data;

@Data
public class ProducerOrderRequestDto {
	private List<String> itemList;
	private Date servingDate;
	private Date reservationDeadline;
	private Long price;
	private Long maxPeopleCount;
	private Long preferenceType;
	private String otherItems;
	private String note;
}
