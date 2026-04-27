package com.example.opscopilot.dto;

import java.time.Instant;
import java.util.List;

/**
 * 会话详情响应。
 */
public record ChatSessionDetailResponse(
        String sessionId,
        String title,
        Instant updatedAt,
        List<MessageItem> messages
) {

    /**
     * 会话消息项。
     */
    public record MessageItem(
            String id,
            String role,
            String content,
            String modelName,
            Instant createdAt
    ) {
    }
}
