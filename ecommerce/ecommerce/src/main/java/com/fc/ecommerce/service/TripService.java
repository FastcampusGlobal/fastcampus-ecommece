package com.fc.ecommerce.service;

import com.fc.ecommerce.model.Trip;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class TripService {

    private final DynamoDbClient dynamoDbClient;
    private final String TABLE_NAME = "location-table"; // Your DynamoDB table name

    public TripService() {
        this.dynamoDbClient = DynamoDbClient.builder()
                .region(Region.US_EAST_1) // Change to your region
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    public Trip createTrip(Trip trip) {
        if (trip.getDriverId() == null || trip.getPassengerId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Driver ID and Passenger ID are required");
        }

        // Generate a unique ID if not provided
        if (trip.getTripId() == null) {
            trip.setTripId(UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE);
        }

        Map<String, AttributeValue> item = new HashMap<>();
        item.put("trip_id", AttributeValue.builder().n(trip.getTripId().toString()).build());
        item.put("driver_id", AttributeValue.builder().n(trip.getDriverId().toString()).build());
        item.put("passenger_id", AttributeValue.builder().n(trip.getPassengerId().toString()).build());
        
        if (trip.getDriverName() != null) {
            item.put("driver_name", AttributeValue.builder().s(trip.getDriverName()).build());
        }
        if (trip.getDriverPhone() != null) {
            item.put("driver_phone", AttributeValue.builder().s(trip.getDriverPhone()).build());
        }
        if (trip.getVehicleId() != null) {
            item.put("vehicle_id", AttributeValue.builder().s(trip.getVehicleId()).build());
        }
        if (trip.getVehicleModel() != null) {
            item.put("vehicle_model", AttributeValue.builder().s(trip.getVehicleModel()).build());
        }
        if (trip.getPassengerName() != null) {
            item.put("passenger_name", AttributeValue.builder().s(trip.getPassengerName()).build());
        }
        if (trip.getPassengerPhone() != null) {
            item.put("passenger_phone", AttributeValue.builder().s(trip.getPassengerPhone()).build());
        }
        if (trip.getStartLatitude() != null) {
            item.put("start_latitude", AttributeValue.builder().n(trip.getStartLatitude().toString()).build());
        }
        if (trip.getStartLongitude() != null) {
            item.put("start_longitude", AttributeValue.builder().n(trip.getStartLongitude().toString()).build());
        }
        if (trip.getTripStatus() != null) {
            item.put("trip_status", AttributeValue.builder().s(trip.getTripStatus()).build());
        }
        if (trip.getRecordedTime() != null) {
            item.put("recorded_time", AttributeValue.builder().s(trip.getRecordedTime()).build());
        }
        if (trip.getEndLatitude() != null) {
            item.put("end_latitude", AttributeValue.builder().n(trip.getEndLatitude().toString()).build());
        }
        if (trip.getEndLongitude() != null) {
            item.put("end_longitude", AttributeValue.builder().n(trip.getEndLongitude().toString()).build());
        }
        if (trip.getAccuracy() != null) {
            item.put("accuracy", AttributeValue.builder().n(trip.getAccuracy().toString()).build());
        }
        if (trip.getCurrentLatitude() != null) {
            item.put("current_latitude", AttributeValue.builder().n(trip.getCurrentLatitude().toString()).build());
        }
        if (trip.getCurrentLongitude() != null) {
            item.put("current_longitude", AttributeValue.builder().n(trip.getCurrentLongitude().toString()).build());
        }
        if (trip.getEndTime() != null) {
            item.put("end_time", AttributeValue.builder().s(trip.getEndTime()).build());
        }
        if (trip.getStartTime() != null) {
            item.put("start_time", AttributeValue.builder().s(trip.getStartTime()).build());
        }
        if (trip.getSpeed() != null) {
            item.put("speed", AttributeValue.builder().n(trip.getSpeed().toString()).build());
        }
        item.put("id", AttributeValue.builder().s(UUID.randomUUID().toString()).build());


        PutItemRequest putItemRequest = PutItemRequest.builder()
                .tableName(TABLE_NAME)
                .item(item)
                .build();

        try {
            dynamoDbClient.putItem(putItemRequest);
            return trip;
        } catch (DynamoDbException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Error saving trip to DynamoDB: " + e.getMessage());
        }
    }

    public Trip getTripById(Long tripId) {
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("trip_id", AttributeValue.builder().n(tripId.toString()).build());

        GetItemRequest getItemRequest = GetItemRequest.builder()
                .tableName(TABLE_NAME)
                .key(key)
                .build();

        try {
            GetItemResponse response = dynamoDbClient.getItem(getItemRequest);
            if (!response.hasItem()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, 
                    "Trip not found with id: " + tripId);
            }
            return mapToTrip(response.item());
        } catch (DynamoDbException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Error retrieving trip from DynamoDB: " + e.getMessage());
        }
    }

    public Trip updateTripStatus(Long tripId, String status) {
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("trip_id", AttributeValue.builder().n(tripId.toString()).build());

        Map<String, AttributeValue> values = new HashMap<>();
        values.put(":status", AttributeValue.builder().s(status).build());
        values.put(":update_time", AttributeValue.builder()
            .s(java.time.LocalDateTime.now().toString()).build());

        UpdateItemRequest updateItemRequest = UpdateItemRequest.builder()
                .tableName(TABLE_NAME)
                .key(key)
                .updateExpression("SET trip_status = :status, recorded_time = :update_time")
                .expressionAttributeValues(values)
                .returnValues(ReturnValue.ALL_NEW)
                .build();

        try {
            UpdateItemResponse response = dynamoDbClient.updateItem(updateItemRequest);
            return mapToTrip(response.attributes());
        } catch (DynamoDbException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Error updating trip status in DynamoDB: " + e.getMessage());
        }
    }

    private Trip mapToTrip(Map<String, AttributeValue> item) {
        Trip trip = new Trip();
        trip.setTripId(Long.parseLong(item.get("trip_id").n()));
        trip.setDriverId(Long.parseLong(item.get("driver_id").n()));
        trip.setPassengerId(Long.parseLong(item.get("passenger_id").n()));
        
        if (item.containsKey("driver_name")) trip.setDriverName(item.get("driver_name").s());
        if (item.containsKey("driver_phone")) trip.setDriverPhone(item.get("driver_phone").s());
        if (item.containsKey("vehicle_id")) trip.setVehicleId(item.get("vehicle_id").s());
        if (item.containsKey("vehicle_model")) trip.setVehicleModel(item.get("vehicle_model").s());
        if (item.containsKey("passenger_name")) trip.setPassengerName(item.get("passenger_name").s());
        if (item.containsKey("passenger_phone")) trip.setPassengerPhone(item.get("passenger_phone").s());
        if (item.containsKey("trip_status")) trip.setTripStatus(item.get("trip_status").s());
        if (item.containsKey("recorded_time")) trip.setRecordedTime(item.get("recorded_time").s());
        if (item.containsKey("end_latitude")) trip.setEndLatitude(Double.parseDouble(item.get("end_latitude").n()));
        if (item.containsKey("end_longitude")) trip.setEndLongitude(Double.parseDouble(item.get("end_longitude").n()));
        if (item.containsKey("accuracy")) trip.setAccuracy(Double.parseDouble(item.get("accuracy").n()));
        if (item.containsKey("current_latitude")) trip.setCurrentLatitude(Double.parseDouble(item.get("current_latitude").n()));
        if (item.containsKey("current_longitude")) trip.setCurrentLongitude(Double.parseDouble(item.get("current_longitude").n()));
        if (item.containsKey("end_time")) trip.setEndTime(item.get("end_time").s());
        if (item.containsKey("start_time")) trip.setStartTime(item.get("start_time").s());
        if (item.containsKey("speed")) trip.setSpeed(Double.parseDouble(item.get("speed").n()));
        
        return trip;
    }
} 