package com.sentinel.anomaly.controller;

import com.sentinel.anomaly.service.AlertService;
import com.sentinel.anomaly.service.SlidingWindowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/anomalies")
@RequiredArgsConstructor
public class AnomalyController {

    private final SlidingWindowService slidingWindowService;
    private final AlertService alertService;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats(
            @RequestParam String eventType,
            @RequestParam String source) {

        long currentCount = slidingWindowService.getCurrentCount(eventType, source);

        Map<String, Object> stats = new HashMap<>();
        stats.put("eventType", eventType);
        stats.put("source", source);
        stats.put("currentCount", currentCount);
        stats.put("windowSize", "60 seconds");

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/alerts")
    public ResponseEntity<List<AlertService.Alert>> getRecentAlerts(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(alertService.getRecentAlerts(limit));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "anomaly-detector");
        return ResponseEntity.ok(health);
    }
}
