package com.fc.ecommerce.controller;

import com.fc.ecommerce.exception.ResourceNotFoundException;
import com.fc.ecommerce.model.Product;
import com.fc.ecommerce.model.Store;
import com.fc.ecommerce.model.User;
import com.fc.ecommerce.repository.ProductRepository;
import com.fc.ecommerce.repository.StoreRepository;
import com.fc.ecommerce.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/v1")
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    // Get all products for a user's store
    @GetMapping("/users/{userId}/store/products")
    public ResponseEntity<List<Product>> getAllProductsByUserId(@PathVariable(value = "userId") Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        Store store = storeRepository.findByOwner(user)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found for user with id: " + userId));

        return ResponseEntity.ok(store.getProducts());
    }

    // Create a new product for a user's store
    @PostMapping("/users/{userId}/store/products")
    public ResponseEntity<Product> createProduct(@PathVariable(value = "userId") Long userId,
                                                 @RequestBody Product productRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        Store store = storeRepository.findByOwner(user)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found for user with id: " + userId));

        productRequest.setStore(store);
        Product product = productRepository.save(productRequest);
        
        // Cache the product after saving
        cacheProduct(product);
        
        return ResponseEntity.ok(product);
    }

    @CachePut(value = "product", key = "#product.id")
    private Product cacheProduct(Product product) {
        return product;
    }

    // Get a single product
    @GetMapping("/products/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable(value = "id") Long productId) {
        Product product = getProductFromCacheOrDatabase(productId);
        return ResponseEntity.ok(product);
    }

    @Cacheable(value = "product", key = "#productId")
    private Product getProductFromCacheOrDatabase(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
    }

    // Update a product
    @PutMapping("/products/{id}")
    @CachePut(value = "product", key = "#productId")
    public ResponseEntity<Product> updateProduct(@PathVariable(value = "id") Long productId,
                                                 @RequestBody Product productDetails) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        product.setName(productDetails.getName());
        product.setDescription(productDetails.getDescription());
        product.setPrice(productDetails.getPrice());

        Product updatedProduct = productRepository.save(product);
        return ResponseEntity.ok(updatedProduct);
    }

    // Delete a product
    @DeleteMapping("/products/{id}")
    @CacheEvict(value = "product", key = "#productId")
    public ResponseEntity<?> deleteProduct(@PathVariable(value = "id") Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        productRepository.delete(product);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/products/search")
    public ResponseEntity<?> searchProductsByName(@RequestParam String name, @RequestParam Long userId) {
        // Rate limiting logic
        String key = "search_rate_limit:" + userId;
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        
        Long count = ops.increment(key);
        if (count == 1) {
            redisTemplate.expire(key, 1, TimeUnit.MINUTES);
        }
        
        if (count > 5) {
            return ResponseEntity.status(429).body("Rate limit exceeded. Try again later.");
        }

        // Existing search logic
        List<Product> products = productRepository.findByNameUsingTrigram(name);
        return ResponseEntity.ok(products);
    }
}