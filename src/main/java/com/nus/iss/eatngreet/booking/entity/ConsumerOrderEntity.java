package com.nus.iss.eatngreet.booking.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@Table(name = "consumer_order")
public class ConsumerOrderEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "consumer_order_id", nullable = false)
	private Long consumerId;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinTable(name = "consumer_producer", joinColumns = @JoinColumn(name = "consumer_order_id"), inverseJoinColumns = @JoinColumn(name = "producer_order_id"))
	private ProducerOrderEntity producerOrderEntity;

	@Column(name = "email")
	private String email;

}
