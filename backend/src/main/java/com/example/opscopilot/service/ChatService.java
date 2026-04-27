package com.example.opscopilot.service;

import com.example.opscopilot.dto.ChatRequest;
import com.example.opscopilot.dto.ChatResponse;
import com.example.opscopilot.entity.ChatMessage;
import com.example.opscopilot.entity.ChatSession;
import com.example.opscopilot.repository.ChatMessageRepository;
import com.example.opscopilot.repository.ChatSessionRepository;
import com.example.opscopilot.security.CurrentUserService;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 对话业务服务，负责会话标识处理、模型调用和对话记录持久化。
 *
 * @author ops-copilot
 * @date 2026/04/21
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ChatService {

    private static final int TITLE_MAX_LENGTH = 32;

    /**
     * 统一的系统提示词，用于约束运维 Copilot 的回答风格和输出结构。
     */
    private static final String SYSTEM_PROMPT = "你是一个面向园区/社区系统的运维 Copilot。回答要结构化、可执行，优先给出排查步骤、风险点和下一步动作。";

    private final ObjectProvider<ChatClient.Builder> chatClientBuilderProvider;
    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final CurrentUserService currentUserService;

    @org.springframework.beans.factory.annotation.Value("${spring.ai.openai.chat.options.model:gpt-4o-mini}")
    private String modelName;

    /**
     * 处理一次对话请求。
     * 先解析或创建会话，再调用模型生成回复，最后把用户消息和助手回复都写入消息表。
     */
    @Transactional
    public ChatResponse chat(ChatRequest request) {
        Long currentUserId = currentUserService.requireCurrentUserId();
        ChatSession session = resolveSession(request.sessionId(), request.message());
        log.info("Chat session resolved: sessionId={}, userId={}, title={}",
                session.getId(),
                session.getUserId(),
                session.getTitle());

        String answer = callModel(request.message());

        saveMessage(session.getId(), "user", request.message(), null);
        saveMessage(session.getId(), "assistant", answer, modelName);
        session.setUpdatedAt(Instant.now());
        chatSessionRepository.save(session);

        log.info("Chat request completed: sessionId={}, answerLength={}, model={}",
                session.getId(),
                answer == null ? 0 : answer.length(),
                modelName);

        return new ChatResponse(String.valueOf(session.getId()), answer, Instant.now());
    }

    /**
     * 封装对 Spring AI ChatClient 的调用。
     * 当模型尚未配置或调用异常时，返回可直接展示给前端的降级提示。
     */
    private String callModel(String message) {
        ChatClient.Builder builder = chatClientBuilderProvider.getIfAvailable();
        if (builder == null) {
            log.warn("Chat model is unavailable because ChatClient.Builder is not configured");
            return "AI 模型尚未配置。请设置 OPENAI_API_KEY 后重启后端服务。";
        }

        try {
            log.info("Calling AI model: model={}, messageLength={}", modelName, message.length());
            return builder.build()
                    .prompt()
                    .system(SYSTEM_PROMPT)
                    .user(message)
                    .call()
                    .content();
        } catch (RuntimeException ex) {
            log.error("AI model invocation failed: model={}, messageLength={}", modelName, message.length(), ex);
            return "AI 调用失败：" + ex.getMessage();
        }
    }

    /**
     * 根据前端传入的 sessionId 复用已有会话。
     * 如果 sessionId 不存在、格式非法，或数据库中查不到对应记录，则自动创建新会话。
     */
    private ChatSession resolveSession(String sessionId, String message) {
        Long currentUserId = currentUserService.requireCurrentUserId();
        if (sessionId != null && !sessionId.isBlank()) {
            try {
                Long id = Long.valueOf(sessionId.trim());
                return chatSessionRepository.findById(id)
                        .filter(session -> session.getUserId().equals(currentUserId)
                                && session.getStatus() != null
                                && session.getStatus() == 1)
                        .map(session -> {
                            log.info("Reusing existing chat session: sessionId={}", id);
                            return session;
                        })
                        .orElseGet(() -> {
                            log.warn("Chat session not found for current user, creating a new one: sessionId={}", id);
                            return createSession(message, currentUserId);
                        });
            } catch (NumberFormatException ignored) {
                log.warn("Invalid sessionId received, creating a new session: sessionId={}", sessionId);
                return createSession(message, currentUserId);
            }
        }
        return createSession(message, currentUserId);
    }

    /**
     * 初始化新会话，使用首条用户消息生成标题，并绑定默认用户。
     */
    private ChatSession createSession(String message, Long currentUserId) {
        ChatSession session = new ChatSession();
        session.setUserId(currentUserId);
        session.setTitle(buildTitle(message));
        session.setStatus((short) 1);
        ChatSession savedSession = chatSessionRepository.save(session);
        log.info("Created new chat session: sessionId={}, userId={}, title={}",
                savedSession.getId(),
                savedSession.getUserId(),
                savedSession.getTitle());
        return savedSession;
    }

    /**
     * 统一写入消息记录，避免用户消息和助手消息的持久化逻辑分散在业务流程里。
     */
    private void saveMessage(Long sessionId, String role, String content, String modelName) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSessionId(sessionId);
        chatMessage.setRole(role);
        chatMessage.setContent(content);
        chatMessage.setModelName(modelName);
        chatMessageRepository.save(chatMessage);
        log.debug("Saved chat message: sessionId={}, role={}, contentLength={}, model={}",
                sessionId,
                role,
                content == null ? 0 : content.length(),
                modelName);
    }

    /**
     * 用用户首条输入生成精简标题，避免会话列表里直接堆整段原文。
     */
    private String buildTitle(String message) {
        if (message == null || message.isBlank()) {
            return "新会话";
        }

        String normalized = message
                .replace('\n', ' ')
                .replace('\r', ' ')
                .replace('\t', ' ')
                .replaceAll("\\s+", " ")
                .strip();

        normalized = normalized
                .replaceAll("^[#>*\\-\\d.\\s]+", "")
                .replaceAll("[`*_~]+", "")
                .strip();

        int sentenceEnd = findSentenceEnd(normalized);
        String title = sentenceEnd > 0 ? normalized.substring(0, sentenceEnd) : normalized;
        title = title.strip();

        if (title.isEmpty()) {
            return "新会话";
        }

        if (title.length() <= TITLE_MAX_LENGTH) {
            return title;
        }

        return title.substring(0, TITLE_MAX_LENGTH).strip() + "...";
    }

    private int findSentenceEnd(String content) {
        int candidate = -1;
        char[] delimiters = new char[]{'。', '！', '？', '.', '!', '?', '；', ';'};
        for (char delimiter : delimiters) {
            int index = content.indexOf(delimiter);
            if (index > 0 && (candidate == -1 || index < candidate)) {
                candidate = index;
            }
        }
        return candidate;
    }
}
