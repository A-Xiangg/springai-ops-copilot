package com.example.opscopilot.dto;

import java.time.Instant;

/**
 * 运维 Copilot 对话响应的数据传输对象。
 *
 * @author ops-copilot
 * @date 2026/04/21
 */
public record ChatResponse(
        String sessionId,
        String answer,
        Instant createdAt
) {
}
