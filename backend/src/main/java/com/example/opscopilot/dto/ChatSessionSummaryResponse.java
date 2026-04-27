package com.example.opscopilot.dto;

import java.time.Instant;

/**
 * 会话列表项响应。
 */
public record ChatSessionSummaryResponse(
        String sessionId,
        String title,
        String lastMessagePreview,
        long messageCount,
        Instant updatedAt
) {
}
