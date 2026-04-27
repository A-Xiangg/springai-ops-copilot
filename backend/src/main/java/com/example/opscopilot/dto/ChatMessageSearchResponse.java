package com.example.opscopilot.dto;

import java.time.Instant;

/**
 * 聊天消息搜索结果。
 */
public record ChatMessageSearchResponse(
        String sessionId,
        String sessionTitle,
        String messageId,
        String role,
        String snippet,
        Instant createdAt
) {
}
