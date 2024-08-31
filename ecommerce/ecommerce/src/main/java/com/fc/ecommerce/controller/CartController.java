package com.fc.ecommerce.controller;

import com.fc.ecommerce.model.Cart;
import com.fc.ecommerce.model.CartRequest;
import com.fc.ecommerce.service.CartService;
import com.fc.ecommerce.service.OrderService;
import com.fc.ecommerce.service.MessageQueueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/v1/carts")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private MessageQueueService messageQueueService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;


    @PostMapping
    public ResponseEntity<Cart> addToCart(@RequestBody CartRequest cartRequest) {
        String token = redisTemplate.opsForValue().get("user_session:" + cartRequest.getUserId());
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        Cart addedItem = cartService.addToCart(cartRequest.getUserId(), cartRequest.getProductId(), cartRequest.getQuantity());
        return ResponseEntity.ok(addedItem);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> removeFromCart(@PathVariable Long id) {
        cartService.removeFromCart(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Cart>> getCartByUserId(@PathVariable Long userId) {
        List<Cart> cartItems = cartService.getCartByUserId(userId);
        return ResponseEntity.ok(cartItems);
    }

    @PostMapping("/checkout/{userId}")
    public ResponseEntity<?> checkoutCart(@PathVariable Long userId) {
        try {
            Cart cart = new Cart();
            cart.setUserId(userId);
            cart.setProductId(userId);
            cart.setQuantity(1);
            cart.setAddedAt(new Date());
            messageQueueService.publishMessage(cart);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Checkout failed: " + e.getMessage());
        }
    }
}