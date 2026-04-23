package com.example.opscopilot.service;

import com.example.opscopilot.dto.ChatRequest;
import com.example.opscopilot.dto.ChatResponse;
import com.example.opscopilot.entity.Conversation;
import com.example.opscopilot.repository.ConversationRepository;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class ChatService {

    private static final String SYSTEM_PROMPT = "你是一个面向园区/社区系统的运维 Copilot。回答要结构化、可执行，优先给出排查步骤、风险点和下一步动作。";

    private final ObjectProvider<ChatClient.Builder> chatClientBuilderProvider;
    private final ConversationRepository conversationRepository;

    @Transactional
    public ChatResponse chat(ChatRequest request) {
        String sessionId = normalizeSessionId(request.sessionId());
        String answer = callModel(request.message());

        Conversation conversation = new Conversation();
        conversation.setSessionId(sessionId);
        conversation.setUserMessage(request.message());
        conversation.setAssistantAnswer(answer);
        conversationRepository.save(conversation);
        return new ChatResponse(sessionId, answer, Instant.now());
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

    private String normalizeSessionId(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return UUID.randomUUID().toString();
        }
        return sessionId.trim();
    }
}
