package com.avatar.avatar_online.pubsub.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class PublisherService {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public void publish(String topic, String message) {
        kafkaTemplate.send(topic, message);
    }

}
