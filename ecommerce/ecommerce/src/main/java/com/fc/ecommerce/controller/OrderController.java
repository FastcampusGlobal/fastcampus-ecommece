package com.fc.ecommerce.controller;

import com.fc.ecommerce.exception.ResourceNotFoundException;
import com.fc.ecommerce.model.Order;
import com.fc.ecommerce.repository.OrderRepository;
import com.fc.ecommerce.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class OrderController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    // Get all orders for a user
    @GetMapping("/users/{userId}/orders")
    public ResponseEntity<List<Order>> getAllOrdersByUserId(@PathVariable(value = "userId") Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        
        List<Order> orders = orderRepository.findByUserId(userId);
        return ResponseEntity.ok(orders);
    }
}