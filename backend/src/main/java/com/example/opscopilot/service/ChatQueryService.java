package com.example.opscopilot.service;

import com.example.opscopilot.dto.ChatSessionDetailResponse;
import com.example.opscopilot.dto.ChatMessageSearchResponse;
import com.example.opscopilot.dto.ChatSessionSummaryResponse;
import com.example.opscopilot.entity.ChatMessage;
import com.example.opscopilot.entity.ChatSession;
import com.example.opscopilot.repository.ChatMessageRepository;
import com.example.opscopilot.repository.ChatSessionRepository;
import com.example.opscopilot.security.CurrentUserService;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * 聊天查询服务，负责会话列表和历史消息读取。
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ChatQueryService {

    private static final int SEARCH_RESULT_LIMIT = 30;

    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final CurrentUserService currentUserService;

    /**
     * 获取默认用户的会话列表。
     */
    @Transactional(readOnly = true)
    public List<ChatSessionSummaryResponse> listSessions() {
        Long currentUserId = currentUserService.requireCurrentUserId();
        List<ChatSessionSummaryResponse> sessions = chatSessionRepository
                .findByUserIdAndStatusOrderByUpdatedAtDesc(currentUserId, (short) 1)
                .stream()
                .map(this::toSessionSummary)
                .toList();

        log.info("Loaded chat sessions: userId={}, sessionCount={}", currentUserId, sessions.size());
        return sessions;
    }

    /**
     * 获取某个会话的历史消息。
     */
    @Transactional(readOnly = true)
    public ChatSessionDetailResponse getSessionDetail(String sessionId) {
        Long currentUserId = currentUserService.requireCurrentUserId();
        Long id = parseSessionId(sessionId);
        ChatSession session = chatSessionRepository.findById(id)
                .filter(item -> item.getUserId().equals(currentUserId) && item.getStatus() != null && item.getStatus() == 1)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "会话不存在"));

        List<ChatSessionDetailResponse.MessageItem> messages = chatMessageRepository
                .findBySessionIdOrderByCreatedAtAsc(id)
                .stream()
                .map(message -> new ChatSessionDetailResponse.MessageItem(
                        String.valueOf(message.getId()),
                        message.getRole(),
                        message.getContent(),
                        message.getModelName(),
                        message.getCreatedAt()
                ))
                .toList();

        log.info("Loaded chat session detail: sessionId={}, messageCount={}", sessionId, messages.size());
        return new ChatSessionDetailResponse(
                String.valueOf(session.getId()),
                titleOrFallback(session.getTitle()),
                session.getUpdatedAt(),
                messages
        );
    }

    /**
     * 按关键字搜索当前用户的历史聊天消息。
     */
    @Transactional(readOnly = true)
    public List<ChatMessageSearchResponse> searchMessages(String keyword) {
        String normalizedKeyword = keyword == null ? "" : keyword.strip();
        if (normalizedKeyword.isEmpty()) {
            return List.of();
        }

        Long currentUserId = currentUserService.requireCurrentUserId();
        List<ChatMessage> matchedMessages = chatMessageRepository.searchByUserIdAndContent(
                currentUserId,
                normalizedKeyword,
                PageRequest.of(0, SEARCH_RESULT_LIMIT)
        );

        Map<Long, ChatSession> sessionMap = chatSessionRepository
                .findAllById(matchedMessages.stream().map(ChatMessage::getSessionId).distinct().toList())
                .stream()
                .filter(session -> session.getUserId().equals(currentUserId) && session.getStatus() != null && session.getStatus() == 1)
                .collect(java.util.stream.Collectors.toMap(ChatSession::getId, Function.identity()));

        List<ChatMessageSearchResponse> results = matchedMessages.stream()
                .map(message -> {
                    ChatSession session = sessionMap.get(message.getSessionId());
                    if (session == null) {
                        return null;
                    }
                    return new ChatMessageSearchResponse(
                            String.valueOf(session.getId()),
                            titleOrFallback(session.getTitle()),
                            String.valueOf(message.getId()),
                            message.getRole(),
                            buildSearchSnippet(message.getContent(), normalizedKeyword),
                            message.getCreatedAt()
                    );
                })
                .filter(java.util.Objects::nonNull)
                .toList();

        log.info("Searched chat messages: userId={}, keywordLength={}, resultCount={}",
                currentUserId,
                normalizedKeyword.length(),
                results.size());
        return results;
    }

    private ChatSessionSummaryResponse toSessionSummary(ChatSession session) {
        List<ChatMessage> messages = chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(session.getId());
        ChatMessage latestMessage = messages.isEmpty() ? null : messages.get(messages.size() - 1);

        return new ChatSessionSummaryResponse(
                String.valueOf(session.getId()),
                titleOrFallback(session.getTitle()),
                latestMessage == null ? "-" : buildPreview(latestMessage.getContent()),
                messages.size(),
                session.getUpdatedAt()
        );
    }

    private Long parseSessionId(String sessionId) {
        try {
            return Long.valueOf(sessionId.trim());
        } catch (RuntimeException ex) {
            throw new ResponseStatusException(NOT_FOUND, "会话不存在");
        }
    }

    private String buildPreview(String content) {
        if (content == null || content.isBlank()) {
            return "-";
        }
        String normalized = content.strip();
        return normalized.length() <= 80 ? normalized : normalized.substring(0, 80);
    }

    private String titleOrFallback(String title) {
        if (title == null || title.isBlank()) {
            return "未命名会话";
        }
        return title.strip();
    }

    private String buildSearchSnippet(String content, String keyword) {
        if (content == null || content.isBlank()) {
            return "-";
        }

        String normalizedContent = content.replace('\n', ' ').strip();
        String lowerContent = normalizedContent.toLowerCase();
        String lowerKeyword = keyword.toLowerCase();
        int matchIndex = lowerContent.indexOf(lowerKeyword);

        if (matchIndex < 0) {
            return normalizedContent.length() <= 120 ? normalizedContent : normalizedContent.substring(0, 120) + "...";
        }

        int snippetStart = Math.max(0, matchIndex - 28);
        int snippetEnd = Math.min(normalizedContent.length(), matchIndex + keyword.length() + 52);
        String snippet = normalizedContent.substring(snippetStart, snippetEnd);
        if (snippetStart > 0) {
            snippet = "..." + snippet;
        }
        if (snippetEnd < normalizedContent.length()) {
            snippet = snippet + "...";
        }
        return snippet;
    }
}
