package com.example.opscopilot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 用户发起对话请求的数据传输对象。
 *
 * @author ops-copilot
 * @date 2026/04/21
 */
public record ChatRequest(
        @NotBlank(message = "message must not be blank")
        @Size(max = 4000, message = "message must be less than 4000 characters")
        String message,
        String sessionId
) {
}
