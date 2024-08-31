package com.fc.ecommerce.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fc.ecommerce.exception.ResourceNotFoundException;
import com.fc.ecommerce.model.Store;
import com.fc.ecommerce.model.User;
import com.fc.ecommerce.repository.StoreRepository;
import com.fc.ecommerce.repository.UserRepository;

@RestController
@RequestMapping("/api/v1/")
public class StoreController {

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private UserRepository userRepository;

    // get all Stores
    @GetMapping("/stores")
    public List<Store> getAllStores() {
        return storeRepository.findAll();
    }

    // create Store rest api
    @PostMapping("/stores")
    public ResponseEntity<?> createStore(@RequestBody Store store, @RequestParam Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not exist with id :" + userId));

        if (user.getStore() != null) {
            return ResponseEntity.badRequest().body("User already has a store");
        }

        store.setOwner(user);
        user.setStore(store);
        Store createdStore = storeRepository.save(store);
        return ResponseEntity.ok(createdStore);
    }

    // get Store by id rest api
    @GetMapping("/stores/{id}")
    public ResponseEntity<Store> getStoreById(@PathVariable Long id) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Store not exist with id :" + id));
        return ResponseEntity.ok(store);
    }

    // update Store rest api
    @PutMapping("/stores/{id}")
    public ResponseEntity<Store> updateStore(@PathVariable Long id, @RequestBody Store storeDetails) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Store not exist with id :" + id));

        store.setName(storeDetails.getName());
        store.setDescription(storeDetails.getDescription());

        Store updatedStore = storeRepository.save(store);
        return ResponseEntity.ok(updatedStore);
    }

    // delete Store rest api
    @DeleteMapping("/stores/{id}")
    public ResponseEntity<Map<String, Boolean>> deleteStore(@PathVariable Long id) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Store not exist with id :" + id));

        User owner = store.getOwner();
        if (owner != null) {
            owner.setStore(null);
            userRepository.save(owner);
        }

        storeRepository.delete(store);
        Map<String, Boolean> response = new HashMap<>();
        response.put("deleted", Boolean.TRUE);
        return ResponseEntity.ok(response);
    }
}