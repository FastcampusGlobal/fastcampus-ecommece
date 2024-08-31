package com.fc.ecommerce.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fc.ecommerce.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long>{

}
