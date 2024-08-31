package com.consumer.consumer;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.QueueDoesNotExistException;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.consumer.consumer.model.Cart;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.consumer.consumer.service.OrderService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageQueueService {
    @Value("${app.config.message.queue.topic}")
    private String messageQueueTopic;

    @Value("${app.config.message.queue.url}")
    private String queueUrl;
    private final AmazonSQS amazonSQSClient;
    private final ObjectMapper objectMapper;
    private final OrderService orderService;

    @Scheduled(fixedDelay = 50)
    public void receiveMessages() {
        try {
            String queueUrl = this.queueUrl;
            ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(queueUrl);
            receiveMessageRequest.setMaxNumberOfMessages(10);

            List<Message> messages = amazonSQSClient.receiveMessage(receiveMessageRequest).getMessages();

            for (Message message : messages) {
                log.info("Incoming Message From SQS {}", message.getMessageId());
                log.info("Message Body {}", message.getBody());
                Cart cart = parseMessageToCart(message.getBody());
                orderService.checkout(cart.getUserId());
                amazonSQSClient.deleteMessage(queueUrl, message.getReceiptHandle());
            }

        } catch (QueueDoesNotExistException e) {
            log.error("Queue does not exist {}", e.getMessage());
        }
    }

     private Cart parseMessageToCart(String messageBody) {
        try {
            return objectMapper.readValue(messageBody, Cart.class);
        } catch (Exception e) {
            log.error("Error parsing message to Cart: {}", e.getMessage());
            return null;
        }
    }
}