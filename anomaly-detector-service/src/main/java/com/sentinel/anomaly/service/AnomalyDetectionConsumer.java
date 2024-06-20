package com.sentinel.anomaly.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnomalyDetectionConsumer {

    private final SlidingWindowService slidingWindowService;
    private final AlertService alertService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "sentinel-events", groupId = "anomaly-detector-group")
    public void detectAnomalies(String eventJson) {
        try {
            Map<String, Object> eventMap = objectMapper.readValue(eventJson, Map.class);

            String eventType = (String) eventMap.get("eventType");
            String source = (String) eventMap.get("source");
            String eventId = (String) eventMap.get("eventId");

            // Add to sliding window and check for anomalies
            boolean isAnomaly = slidingWindowService.addEventAndCheckAnomaly(eventType, source);

            if (isAnomaly) {
                long currentCount = slidingWindowService.getCurrentCount(eventType, source);
                alertService.sendAlert(eventType, source, currentCount);

                log.warn("ANOMALY ALERT: Event type '{}' from source '{}' - {} events in window",
                        eventType, source, currentCount);
            }

        } catch (Exception e) {
            log.error("Failed to process event for anomaly detection", e);
        }
    }
}
