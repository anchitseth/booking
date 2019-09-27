package com.nus.iss.eatngreet.booking.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.nus.iss.eatngreet.booking.entity.ItemEntity;

@Repository
public interface ItemRepository extends JpaRepository<ItemEntity, Long> {

	@Query("Select i from ItemEntity i where i.name like %:name% and isDeleted=false and isActive=true")
	List<ItemEntity> itemNameAutoSuggest(@Param("name") String itemName, Pageable pageable);

	List<ItemEntity> findByItemId(Long itemId);
	
	List<ItemEntity> findByName(String name);

}
