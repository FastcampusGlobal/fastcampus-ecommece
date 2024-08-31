package com.consumer.consumer.repository;

import com.consumer.consumer.model.Store;
import com.consumer.consumer.model.User;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;



@Repository
public interface StoreRepository extends JpaRepository<Store, Long>{
    Optional<Store> findByOwner(User user);
}