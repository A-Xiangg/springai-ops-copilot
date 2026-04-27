package com.example.opscopilot.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 为每个请求补齐 traceId，并统一输出入口/出口日志。
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceLoggingFilter extends OncePerRequestFilter {

    public static final String TRACE_ID_HEADER = "X-Trace-Id";
    private static final String TRACE_ID_KEY = "traceId";
    private static final int MAX_PAYLOAD_LENGTH = 4000;
    private static final Set<String> EXCLUDED_PATH_PREFIXES = Set.of("/actuator", "/swagger-ui", "/v3/api-docs");
    private static final Set<String> MASKED_FIELDS = Set.of("password", "token", "authorization", "apiKey", "api_key");

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return EXCLUDED_PATH_PREFIXES.stream().anyMatch(path::startsWith);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String traceId = resolveTraceId(request);
        long startNanos = System.nanoTime();
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        MDC.put(TRACE_ID_KEY, traceId);
        responseWrapper.setHeader(TRACE_ID_HEADER, traceId);

        log.info("HTTP request started: method={}, uri={}, query={}, clientIp={}, params={}",
                requestWrapper.getMethod(),
                requestWrapper.getRequestURI(),
                sanitizeQuery(requestWrapper.getQueryString()),
                resolveClientIp(requestWrapper),
                sanitizeParameters(requestWrapper.getParameterMap()));

        try {
            filterChain.doFilter(requestWrapper, responseWrapper);
        } finally {
            long durationMs = (System.nanoTime() - startNanos) / 1_000_000;
            log.info("HTTP request payload: method={}, uri={}, contentType={}, body={}",
                    requestWrapper.getMethod(),
                    requestWrapper.getRequestURI(),
                    requestWrapper.getContentType(),
                    extractRequestBody(requestWrapper));
            log.info("HTTP response completed: method={}, uri={}, status={}, durationMs={}, contentType={}, body={}",
                    requestWrapper.getMethod(),
                    requestWrapper.getRequestURI(),
                    responseWrapper.getStatus(),
                    durationMs,
                    responseWrapper.getContentType(),
                    extractResponseBody(responseWrapper));
            responseWrapper.copyBodyToResponse();
            MDC.remove(TRACE_ID_KEY);
        }
    }

    private String resolveTraceId(HttpServletRequest request) {
        String traceId = request.getHeader(TRACE_ID_HEADER);
        if (traceId == null || traceId.isBlank()) {
            return UUID.randomUUID().toString().replace("-", "");
        }
        return traceId.trim();
    }

    private String sanitizeQuery(String query) {
        return query == null || query.isBlank() ? "-" : query;
    }

    private String sanitizeParameters(Map<String, String[]> parameterMap) {
        if (parameterMap == null || parameterMap.isEmpty()) {
            return "-";
        }

        StringBuilder builder = new StringBuilder();
        parameterMap.forEach((key, values) -> {
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append(key).append('=');
            if (MASKED_FIELDS.contains(key)) {
                builder.append("***");
            } else {
                builder.append(Arrays.toString(values));
            }
        });
        return truncate(builder.toString());
    }

    private String extractRequestBody(ContentCachingRequestWrapper request) {
        byte[] content = request.getContentAsByteArray();
        if (content.length == 0) {
            return "-";
        }
        if (!isVisibleContent(request.getContentType())) {
            return "[binary content omitted]";
        }

        String body = new String(content, 0, Math.min(content.length, MAX_PAYLOAD_LENGTH), StandardCharsets.UTF_8);
        return truncate(maskSensitiveFields(body));
    }

    private String extractResponseBody(ContentCachingResponseWrapper response) {
        byte[] content = response.getContentAsByteArray();
        if (content.length == 0) {
            return "-";
        }
        if (!isVisibleContent(response.getContentType())) {
            return "[binary content omitted]";
        }

        String body = new String(content, 0, Math.min(content.length, MAX_PAYLOAD_LENGTH), StandardCharsets.UTF_8);
        return truncate(maskSensitiveFields(body));
    }

    private boolean isVisibleContent(String contentType) {
        if (!StringUtils.hasText(contentType)) {
            return true;
        }

        return contentType.contains(MediaType.APPLICATION_JSON_VALUE)
                || contentType.contains(MediaType.APPLICATION_XML_VALUE)
                || contentType.contains(MediaType.TEXT_PLAIN_VALUE)
                || contentType.contains(MediaType.TEXT_HTML_VALUE)
                || contentType.contains(MediaType.APPLICATION_FORM_URLENCODED_VALUE);
    }

    private String maskSensitiveFields(String value) {
        String masked = value;
        for (String field : MASKED_FIELDS) {
            masked = masked.replaceAll("(\"" + field + "\"\\s*:\\s*\")(.*?)(\")", "$1***$3");
            masked = masked.replaceAll("(" + field + "=)([^&\\s]+)", "$1***");
        }
        return masked;
    }

    private String truncate(String value) {
        if (value == null || value.isBlank()) {
            return "-";
        }
        return value.length() <= MAX_PAYLOAD_LENGTH ? value : value.substring(0, MAX_PAYLOAD_LENGTH) + "...(truncated)";
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
