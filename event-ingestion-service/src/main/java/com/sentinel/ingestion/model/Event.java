package com.sentinel.ingestion.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Event {

    @NotBlank(message = "Event ID cannot be blank")
    private String eventId;

    @NotBlank(message = "Event type cannot be blank")
    private String eventType;

    @NotBlank(message = "Source cannot be blank")
    private String source;

    @NotNull(message = "Timestamp cannot be null")
    private Instant timestamp;

    private String severity;

    private Map<String, Object> payload;

    private Map<String, String> metadata;
}
