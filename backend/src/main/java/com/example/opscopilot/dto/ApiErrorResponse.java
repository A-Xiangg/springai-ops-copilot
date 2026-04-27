package com.example.opscopilot.dto;

import java.time.Instant;

/**
 * 统一错误响应体。
 */
public record ApiErrorResponse(
        int status,
        String error,
        String message,
        String path,
        Instant timestamp
) {
}
