package com.fc.ecommerce.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fc.ecommerce.exception.ResourceNotFoundException;
import com.fc.ecommerce.model.User;
import com.fc.ecommerce.repository.UserRepository;

@RestController
@RequestMapping("/api/v1/")
public class UserController {

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private RedisTemplate<String, String> redisTemplate;
	
	// get all Users
	@GetMapping("/users")
	public List<User> getAllUsers(){
		return userRepository.findAll();
	}		
	
	// create User rest api
	@PostMapping("/users")
	public ResponseEntity<?> createUser(@RequestBody User User) {
		User savedUser = userRepository.save(User);
		
		// Generate a unique token
		String token = UUID.randomUUID().toString();
		
		// Store the token in Redis with a TTL of 1 hour
		redisTemplate.opsForValue().set("user_session:" +  String.valueOf(savedUser.getId()), token, Duration.ofHours(1));
		
		// Create a response object
		Map<String, Object> response = new HashMap<>();
		response.put("user", savedUser);
		response.put("token", token);
		
		return ResponseEntity.ok(response);
	}
	
	// get User by id rest api
	@GetMapping("/users/{id}")
	public ResponseEntity<User> getUserById(@PathVariable Long id) {
		User User = userRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("User not exist with id :" + id));
		return ResponseEntity.ok(User);
	}
	

	// update User rest api
	
	@PutMapping("/users/{id}")
	public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User UserDetails){
		User User = userRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("User not exist with id :" + id));

		User.setName(UserDetails.getName());
		User.setAge(UserDetails.getAge());
		User.setGender(UserDetails.getGender());
		
		User updatedUser = userRepository.save(User);
		return ResponseEntity.ok(updatedUser);
	}
	
	// delete User rest api
	@DeleteMapping("/users/{id}")
	public ResponseEntity<Map<String, Boolean>> deleteUser(@PathVariable Long id){
		User User = userRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("User not exist with id :" + id));
		
		userRepository.delete(User);
		Map<String, Boolean> response = new HashMap<>();
		response.put("deleted", Boolean.TRUE);
		return ResponseEntity.ok(response);
	}
}