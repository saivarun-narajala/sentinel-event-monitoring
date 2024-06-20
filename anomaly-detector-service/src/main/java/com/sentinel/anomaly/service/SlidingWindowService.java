package com.sentinel.anomaly.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis-based sliding window implementation for anomaly detection.
 * Uses sorted sets to maintain time-ordered event counts within a time window.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SlidingWindowService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final long WINDOW_SIZE_SECONDS = 60; // 1 minute window
    private static final long THRESHOLD = 100; // Alert if more than 100 events per minute

    /**
     * Add an event to the sliding window and check if threshold is exceeded.
     * 
     * @param eventType The type of event
     * @param source    The source of the event
     * @return true if anomaly detected (threshold exceeded)
     */
    public boolean addEventAndCheckAnomaly(String eventType, String source) {
        String key = buildKey(eventType, source);
        long now = Instant.now().toEpochMilli();

        // Add current event with timestamp as score
        redisTemplate.opsForZSet().add(key, String.valueOf(now), now);

        // Remove events outside the sliding window
        long windowStart = now - (WINDOW_SIZE_SECONDS * 1000);
        redisTemplate.opsForZSet().removeRangeByScore(key, 0, windowStart);

        // Set expiration to clean up old keys
        redisTemplate.expire(key, WINDOW_SIZE_SECONDS * 2, TimeUnit.SECONDS);

        // Count events in current window
        Long count = redisTemplate.opsForZSet().zCard(key);

        if (count != null && count > THRESHOLD) {
            log.warn("Anomaly detected! {} events of type {} from source {} in last {} seconds",
                    count, eventType, source, WINDOW_SIZE_SECONDS);
            return true;
        }

        return false;
    }

    /**
     * Get the current event count within the sliding window.
     */
    public long getCurrentCount(String eventType, String source) {
        String key = buildKey(eventType, source);
        Long count = redisTemplate.opsForZSet().zCard(key);
        return count != null ? count : 0;
    }

    /**
     * Get event count for a specific time range.
     */
    public long getCountInRange(String eventType, String source, Instant start, Instant end) {
        String key = buildKey(eventType, source);
        Long count = redisTemplate.opsForZSet().count(
                key,
                start.toEpochMilli(),
                end.toEpochMilli());
        return count != null ? count : 0;
    }

    private String buildKey(String eventType, String source) {
        return String.format("sliding_window:%s:%s", eventType, source);
    }
}
