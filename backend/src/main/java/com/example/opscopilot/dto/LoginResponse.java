package com.example.opscopilot.dto;

import java.time.Instant;

/**
 * 登录响应结果。
 */
public record LoginResponse(
        Long userId,
        String username,
        String nickname,
        String token,
        Instant loginAt
) {
}
