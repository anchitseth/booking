package com.nus.iss.eatngreet.booking.responsedto;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import com.nus.iss.eatngreet.booking.entity.ItemEntity;

import lombok.Data;

@Data
public class MyConsumerOrdersResponseDto {
	private Set<ItemEntity> itemList;
	private Date servingDate;
	private Float price;
	private Long preference;
	private String firstName;
	private String lastName;
	private Object address;
}
