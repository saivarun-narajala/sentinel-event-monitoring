package com.sentinel.ingestion.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentinel.ingestion.model.Event;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventPublisher {
    
    private static final String TOPIC = "sentinel-events";
    
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    
    public void publishEvent(Event event) {
        try {
            String eventJson = objectMapper.writeValueAsString(event);
            
            CompletableFuture<SendResult<String, String>> future = 
                kafkaTemplate.send(TOPIC, event.getEventId(), eventJson);
            
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.debug("Event published to Kafka: {} at offset {}", 
                        event.getEventId(), 
                        result.getRecordMetadata().offset());
                } else {
                    log.error("Failed to publish event: {}", event.getEventId(), ex);
                }
            });
            
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize event: {}", event.getEventId(), e);
            throw new RuntimeException("Event serialization failed", e);
        }
    }
}
