package com.fc.ecommerce.service;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.*;
import com.fc.ecommerce.model.Cart;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageQueueService {

    @Value("${app.config.message.queue.topic}")
    private String messageQueueTopic;
    @Value("${app.config.message.queue.url}")
    private String queueUrl;
    private final AmazonSQS amazonSQSClient;

    public void createMessageQueue() {
        log.info("Creating message queue on AWS SQS");
    }

    public void publishMessage(Cart createExpenseDto) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonPayload = objectMapper.writeValueAsString(createExpenseDto);

            SendMessageRequest sendMessageRequest = new SendMessageRequest()
            .withQueueUrl(this.queueUrl)
            .withMessageBody(jsonPayload);

            amazonSQSClient.sendMessage(sendMessageRequest);
        } catch (InvalidMessageContentsException | JsonProcessingException e) {
            log.error("Queue does not exist {}", e.getMessage());
        }

    }
}