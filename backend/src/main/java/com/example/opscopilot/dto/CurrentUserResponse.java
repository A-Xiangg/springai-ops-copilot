package com.example.opscopilot.dto;

import java.time.Instant;

/**
 * 当前登录用户信息。
 */
public record CurrentUserResponse(
        Long userId,
        String username,
        String nickname,
        Instant loginAt
) {
}
