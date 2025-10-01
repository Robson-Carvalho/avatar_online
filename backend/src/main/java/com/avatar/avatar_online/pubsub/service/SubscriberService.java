package com.avatar.avatar_online.pubsub.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class SubscriberService {

    @KafkaListener(topics = "my-topic", groupId = "myGroup")
    public void listen(String message) {
        System.out.println("Received message: " + message);
    }

}
