package com.avatar.avatar_online.pubsub.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class PublisherService {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;


    public void publish(String topic, String message, String key) {
        kafkaTemplate.send(topic, key, message).whenComplete((result, ex) -> {
            if (ex == null) {
                System.out.println("✅ SUCESSO na Publicação! Tópico: " + topic +
                        " | Partição: " + result.getRecordMetadata().partition() +
                        " | Chave (Key): " + key);
            } else {
                System.err.println("❌ FALHA na Publicação! Tópico: " + topic +
                        " | Motivo: " + ex.getMessage());
            }
        });
    }
}
