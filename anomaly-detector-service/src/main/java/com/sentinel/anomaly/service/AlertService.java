package com.sentinel.anomaly.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class AlertService {

    // In-memory alert tracking (in production, this would be a separate alerting
    // system)
    private final Map<String, List<Alert>> alerts = new ConcurrentHashMap<>();

    public void sendAlert(String eventType, String source, long eventCount) {
        Alert alert = new Alert(
                eventType,
                source,
                eventCount,
                Instant.now(),
                String.format("Abnormal event pattern detected: %d events from %s of type %s",
                        eventCount, source, eventType));

        String key = eventType + ":" + source;
        alerts.computeIfAbsent(key, k -> new ArrayList<>()).add(alert);

        // In production, this would send to monitoring systems like PagerDuty, Slack,
        // etc.
        log.error("🚨 ALERT: {}", alert.message);
    }

    public List<Alert> getRecentAlerts(int limit) {
        return alerts.values().stream()
                .flatMap(List::stream)
                .sorted((a, b) -> b.timestamp.compareTo(a.timestamp))
                .limit(limit)
                .toList();
    }

    public record Alert(
            String eventType,
            String source,
            long eventCount,
            Instant timestamp,
            String message) {
    }
}
