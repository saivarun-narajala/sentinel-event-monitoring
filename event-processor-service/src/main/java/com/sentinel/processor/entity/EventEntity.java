package com.sentinel.processor.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "events", indexes = {
        @Index(name = "idx_event_type", columnList = "eventType"),
        @Index(name = "idx_timestamp", columnList = "timestamp"),
        @Index(name = "idx_source", columnList = "source")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventEntity {

    @Id
    private String eventId;

    @Column(nullable = false)
    private String eventType;

    @Column(nullable = false)
    private String source;

    @Column(nullable = false)
    private Instant timestamp;

    private String severity;

    @Column(columnDefinition = "TEXT")
    private String payload;

    @Column(columnDefinition = "TEXT")
    private String metadata;

    @Column(nullable = false)
    private Instant processedAt;
}
