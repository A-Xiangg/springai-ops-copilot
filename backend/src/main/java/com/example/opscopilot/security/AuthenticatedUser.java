package com.example.opscopilot.security;

import java.time.Instant;

/**
 * 已认证用户上下文。
 */
public record AuthenticatedUser(
        Long userId,
        String username,
        String nickname,
        Instant loginAt,
        String token
) {
}
