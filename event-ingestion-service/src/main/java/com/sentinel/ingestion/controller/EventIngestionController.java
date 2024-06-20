package com.sentinel.ingestion.controller;

import com.sentinel.ingestion.model.Event;
import com.sentinel.ingestion.service.EventPublisher;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventIngestionController {

    private final EventPublisher eventPublisher;

    @PostMapping
    public ResponseEntity<Map<String, String>> ingestEvent(@Valid @RequestBody Event event) {
        try {
            // Ensure event has an ID and timestamp
            if (event.getEventId() == null || event.getEventId().isBlank()) {
                event.setEventId(UUID.randomUUID().toString());
            }
            if (event.getTimestamp() == null) {
                event.setTimestamp(Instant.now());
            }

            eventPublisher.publishEvent(event);

            log.info("Event ingested successfully: {}", event.getEventId());

            Map<String, String> response = new HashMap<>();
            response.put("eventId", event.getEventId());
            response.put("status", "accepted");
            response.put("message", "Event queued for processing");

            return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);

        } catch (Exception e) {
            log.error("Failed to ingest event", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Failed to process event");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "event-ingestion");
        return ResponseEntity.ok(health);
    }
}
