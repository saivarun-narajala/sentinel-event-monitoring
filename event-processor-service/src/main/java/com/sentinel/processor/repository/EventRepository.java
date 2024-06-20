package com.sentinel.processor.repository;

import com.sentinel.processor.entity.EventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<EventEntity, String> {

    List<EventEntity> findByEventTypeAndTimestampBetween(
            String eventType, Instant start, Instant end);

    List<EventEntity> findBySourceAndTimestampAfter(String source, Instant after);

    @Query("SELECT e FROM EventEntity e WHERE e.severity = 'CRITICAL' AND e.timestamp > :since")
    List<EventEntity> findCriticalEventsSince(Instant since);

    long countByEventTypeAndTimestampBetween(String eventType, Instant start, Instant end);
}
