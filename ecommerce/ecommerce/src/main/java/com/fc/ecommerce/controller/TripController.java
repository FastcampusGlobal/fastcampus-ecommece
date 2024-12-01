package com.fc.ecommerce.controller;

import com.fc.ecommerce.model.Trip;
import com.fc.ecommerce.service.TripService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/trips")
public class TripController {

    @Autowired
    private TripService tripService;

    @PostMapping
    public ResponseEntity<Trip> createTrip(@RequestBody Trip trip) {
        // You might want to set some default values here
        if (trip.getTripStatus() == null) {
            trip.setTripStatus("CREATED");
        }
        if (trip.getRecordedTime() == null) {
            trip.setRecordedTime(java.time.LocalDateTime.now().toString());
        }
        
        tripService.createTrip(trip);
        return ResponseEntity.ok(trip);
    }

    @GetMapping("/{tripId}")
    public ResponseEntity<Trip> getTripById(@PathVariable Long tripId) {
        Trip trip = tripService.getTripById(tripId);
        return ResponseEntity.ok(trip);
    }

    @PutMapping("/{tripId}/status")
    public ResponseEntity<Trip> updateTripStatus(
            @PathVariable Long tripId,
            @RequestParam String status) {
        Trip updatedTrip = tripService.updateTripStatus(tripId, status);
        return ResponseEntity.ok(updatedTrip);
    }
} 
