package com.nus.iss.eatngreet.booking.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.nus.iss.eatngreet.booking.entity.ConsumerOrderEntity;

@Repository
public interface ConsumerOrderRepository extends CrudRepository<ConsumerOrderEntity, Long> {

	public List<ConsumerOrderEntity> findByEmail(String email);
}
