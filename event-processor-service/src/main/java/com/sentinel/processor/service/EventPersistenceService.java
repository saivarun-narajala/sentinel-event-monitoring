package com.sentinel.processor.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentinel.processor.entity.EventEntity;
import com.sentinel.processor.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventPersistenceService {

    private final EventRepository eventRepository;
    private final ObjectMapper objectMapper;

    @Async
    @Transactional
    public void persistEventAsync(Map<String, Object> eventMap) {
        try {
            EventEntity entity = EventEntity.builder()
                    .eventId((String) eventMap.get("eventId"))
                    .eventType((String) eventMap.get("eventType"))
                    .source((String) eventMap.get("source"))
                    .timestamp(parseTimestamp(eventMap.get("timestamp")))
                    .severity((String) eventMap.get("severity"))
                    .payload(serializePayload(eventMap.get("payload")))
                    .metadata(serializePayload(eventMap.get("metadata")))
                    .processedAt(Instant.now())
                    .build();

            eventRepository.save(entity);

            log.debug("Event persisted to PostgreSQL: {}", entity.getEventId());

        } catch (Exception e) {
            log.error("Failed to persist event to database", e);
        }
    }

    private Instant parseTimestamp(Object timestamp) {
        if (timestamp instanceof String) {
            return Instant.parse((String) timestamp);
        } else if (timestamp instanceof Number) {
            return Instant.ofEpochMilli(((Number) timestamp).longValue());
        }
        return Instant.now();
    }

    private String serializePayload(Object payload) {
        if (payload == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize payload", e);
            return payload.toString();
        }
    }
}
