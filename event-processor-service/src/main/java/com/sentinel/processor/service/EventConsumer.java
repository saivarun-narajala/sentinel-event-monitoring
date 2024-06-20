package com.sentinel.processor.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentinel.processor.entity.EventEntity;
import com.sentinel.processor.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventConsumer {

    private final EventRepository eventRepository;
    private final EventPersistenceService persistenceService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "sentinel-events", groupId = "event-processor-group")
    public void consumeEvent(String eventJson) {
        try {
            log.debug("Received event from Kafka");

            Map<String, Object> eventMap = objectMapper.readValue(eventJson, Map.class);

            // Trigger async persistence to avoid blocking Kafka consumer
            persistenceService.persistEventAsync(eventMap);

        } catch (Exception e) {
            log.error("Failed to process event from Kafka", e);
        }
    }
}
