package com.nus.iss.eatngreet.booking.entity;

import java.util.Date;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.CreationTimestamp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "producer_order")
public class ProducerOrderEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "producer_order_id")
	private Long producerOrderId;

	@Column(name = "email")
	private String email;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinTable(name = "producer_item", joinColumns = @JoinColumn(name = "producer_order_id"), inverseJoinColumns = @JoinColumn(name = "item_id"))
	@Column(name = "item_list")
	private Set<ItemEntity> itemList;

	@Column(name = "serving_date")
	private Date servingDate;

	@Column(name = "payment_deadline")
	private Date paymentDeadline;

	@Column(name = "reservation_deadline")
	private Date reservationDeadline;

	@Column(name = "price")
	private Long price;

	@Column(name = "max_people_count")
	private Long maxPeopleCount;

	@Column(name = "actual_people_count")
	private Long actualPeopleCount = 0L;

	@Column(name = "preference_type")
	private Long preferenceType;

	@Column(name = "other_items")
	private String otherItems;

	@Column(name = "note")
	private String note;

	@Column(name = "is_active", columnDefinition = "TINYINT(1) default 1")
	private Boolean isActive;

	@Column(name = "is_deleted", columnDefinition = "TINYINT(1) default 0")
	private Boolean isDeleted;

	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "created_on", length = 19, columnDefinition = "TIMESTAMP default CURRENT_TIMESTAMP")
	private Date createdOn;
}
