package com.nus.iss.eatngreet.booking.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.nus.iss.eatngreet.booking.entity.ProducerOrderEntity;

@Repository
public interface ProducerOrderRepository extends CrudRepository<ProducerOrderEntity, Long> {

	@Query("select p from ProducerOrderEntity p where p.servingDate >= CURRENT_DATE and p.reservationDeadline >= CURRENT_DATE and p.isActive = true and p.isDeleted = false and p.actualPeopleCount < p.maxPeopleCount")
	List<ProducerOrderEntity> findAllProducerRecords(Pageable pageable);

	List<ProducerOrderEntity> findByEmail(String email);

	List<ProducerOrderEntity> findByProducerOrderId(Long producerOrderId);

}
