package com.example.opscopilot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatRequest(
        @NotBlank(message = "message must not be blank")
        @Size(max = 4000, message = "message must be less than 4000 characters")
        String message,
        String sessionId
) {
}
