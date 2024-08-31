package com.fc.ecommerce.service;

import com.fc.ecommerce.model.Cart;
import com.fc.ecommerce.model.Order;
import com.fc.ecommerce.model.Product;
import com.fc.ecommerce.repository.CartRepository;
import com.fc.ecommerce.repository.OrderRepository;
import com.fc.ecommerce.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    @Transactional
    public Order checkout(Long userId) {
       return null;
    }

    private BigDecimal calculateTotalAmount(List<Cart> cartItems) {
        return cartItems.stream()
            .map(item -> {
                Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));
                return product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            })
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<Order> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId);
    }
}