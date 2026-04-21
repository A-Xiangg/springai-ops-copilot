package com.example.opscopilot.dto;

import java.time.Instant;

public record ChatResponse(
        String sessionId,
        String answer,
        Instant createdAt
) {
}
