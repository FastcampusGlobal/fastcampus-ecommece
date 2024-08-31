package com.consumer.consumer.service;

import com.consumer.consumer.model.Cart;
import com.consumer.consumer.model.Order;
import com.consumer.consumer.model.Product;
import com.consumer.consumer.exception.InsufficientStockException;
import com.consumer.consumer.repository.CartRepository;
import com.consumer.consumer.repository.OrderRepository;
import com.consumer.consumer.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
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
        List<Cart> cartItems = cartRepository.findByUserId(userId);
        
        if (cartItems.isEmpty()) {
          log.info("Cart is empty");
          return null;
        }
        BigDecimal totalAmount;

        try {
            totalAmount = calculateTotalAmount(cartItems);
        } catch (InsufficientStockException e) {
            log.error("Insufficient stock during checkout: {}", e.getMessage());
            return null;
        }        
        // Create a list of products from cart items
        List<Product> orderProducts = cartItems.stream()
            .map(item -> productRepository.findById(item.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found")))
            .collect(Collectors.toList());

        log.info("Order products: {}", orderProducts);
        
        // Create order with userId, totalAmount, and products
        Order order = new Order(userId, totalAmount, orderProducts);
        
        Order savedOrder = orderRepository.save(order);

        // Clear the user's cart after successful checkout
        cartRepository.deleteByUserId(userId);

        return savedOrder;
    }

    @Transactional
    private BigDecimal calculateTotalAmount(List<Cart> cartItems) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (Cart item : cartItems) {
            Product product = productRepository.findById(item.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));
            
            if (product.getStock() < item.getQuantity()) {
                throw new InsufficientStockException("Insufficient stock for product: " + product.getName());
            }
            
            BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);
            
            // Reduce stock
            product.setStock(product.getStock() - item.getQuantity());
            productRepository.save(product);
        }
        return totalAmount;
    }

    public List<Order> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId);
    }
}