package com.fc.ecommerce.repository;

import com.fc.ecommerce.model.Store;
import com.fc.ecommerce.model.User;
import java.util.Optional;

import com.fc.ecommerce.repository.StoreRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;



@Repository
public interface StoreRepository extends JpaRepository<Store, Long>{
    Optional<Store> findByOwner(User user);
}