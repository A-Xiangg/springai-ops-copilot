package com.example.opscopilot.service;

import com.example.opscopilot.dto.ChatRequest;
import com.example.opscopilot.dto.ChatResponse;
import com.example.opscopilot.entity.ChatMessage;
import com.example.opscopilot.entity.ChatSession;
import com.example.opscopilot.repository.ChatMessageRepository;
import com.example.opscopilot.repository.ChatSessionRepository;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
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
@RequiredArgsConstructor
public class ChatService {

    private static final String SYSTEM_PROMPT = "你是一个面向园区/社区系统的运维 Copilot。回答要结构化、可执行，优先给出排查步骤、风险点和下一步动作。";

    private final ObjectProvider<ChatClient.Builder> chatClientBuilderProvider;
    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;

    @Value("${app.chat.default-user-id:1}")
    private Long defaultUserId;

    @Value("${spring.ai.openai.chat.options.model:gpt-4o-mini}")
    private String modelName;

    @Transactional
    public ChatResponse chat(ChatRequest request) {
        ChatSession session = resolveSession(request.sessionId(), request.message());
        String answer = callModel(request.message());

        saveMessage(session.getId(), "user", request.message(), null);
        saveMessage(session.getId(), "assistant", answer, modelName);
        session.setUpdatedAt(Instant.now());
        chatSessionRepository.save(session);

        return new ChatResponse(String.valueOf(session.getId()), answer, Instant.now());
    }

    private String callModel(String message) {
        ChatClient.Builder builder = chatClientBuilderProvider.getIfAvailable();
        if (builder == null) {
            return "AI 模型尚未配置。请设置 OPENAI_API_KEY 后重启后端服务。";
        }

        try {
            return builder.build()
                    .prompt()
                    .system(SYSTEM_PROMPT)
                    .user(message)
                    .call()
                    .content();
        } catch (RuntimeException ex) {
            return "AI 调用失败：" + ex.getMessage();
        }
    }

    private ChatSession resolveSession(String sessionId, String message) {
        if (sessionId != null && !sessionId.isBlank()) {
            try {
                Long id = Long.valueOf(sessionId.trim());
                return chatSessionRepository.findById(id)
                        .orElseGet(() -> createSession(message));
            } catch (NumberFormatException ignored) {
                return createSession(message);
            }
        }
        return createSession(message);
    }

    private ChatSession createSession(String message) {
        ChatSession session = new ChatSession();
        session.setUserId(defaultUserId);
        session.setTitle(buildTitle(message));
        session.setStatus((short) 1);
        return chatSessionRepository.save(session);
    }

    private void saveMessage(Long sessionId, String role, String content, String modelName) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSessionId(sessionId);
        chatMessage.setRole(role);
        chatMessage.setContent(content);
        chatMessage.setModelName(modelName);
        chatMessageRepository.save(chatMessage);
    }

    private String buildTitle(String message) {
        String title = message.strip();
        if (title.length() <= 60) {
            return title;
        }
        return title.substring(0, 60);
    }
}
