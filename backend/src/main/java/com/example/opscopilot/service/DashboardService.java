package com.example.opscopilot.service;

import com.example.opscopilot.dto.DashboardSummaryResponse;
import com.example.opscopilot.entity.ChatMessage;
import com.example.opscopilot.entity.ChatSession;
import com.example.opscopilot.repository.ChatMessageRepository;
import com.example.opscopilot.repository.ChatSessionRepository;
import com.example.opscopilot.repository.KnowledgeBaseRepository;
import com.example.opscopilot.security.CurrentUserService;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 首页概览服务。
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DashboardService {

    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("MM-dd HH:mm", Locale.SIMPLIFIED_CHINESE)
                    .withZone(ZoneId.of("Asia/Shanghai"));

    private final KnowledgeBaseRepository knowledgeBaseRepository;
    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final CurrentUserService currentUserService;

    /**
     * 获取首页统计和最近消息。
     */
    @Transactional(readOnly = true)
    public DashboardSummaryResponse getSummary() {
        Long currentUserId = currentUserService.requireCurrentUserId();
        long knowledgeBaseCount = knowledgeBaseRepository.countByStatus((short) 1);
        List<ChatSession> sessions = chatSessionRepository.findByUserIdAndStatusOrderByUpdatedAtDesc(currentUserId, (short) 1);
        long sessionCount = chatSessionRepository.countByUserIdAndStatus(currentUserId, (short) 1);

        List<RecentMessageCandidate> recentMessageCandidates = new ArrayList<>();
        for (ChatSession session : sessions.stream().limit(10).toList()) {
            List<ChatMessage> messages = chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(session.getId());
            if (messages.isEmpty()) {
                continue;
            }

            ChatMessage latestMessage = messages.get(messages.size() - 1);
            recentMessageCandidates.add(new RecentMessageCandidate(
                    String.valueOf(latestMessage.getId()),
                    String.valueOf(session.getId()),
                    titleOrFallback(session.getTitle()),
                    buildPreview(latestMessage.getContent()),
                    latestMessage.getCreatedAt()
            ));
        }

        List<DashboardSummaryResponse.RecentMessageItem> recentMessages = recentMessageCandidates.stream()
                .sorted(Comparator.comparing(RecentMessageCandidate::createdAt).reversed())
                .limit(5)
                .map(item -> new DashboardSummaryResponse.RecentMessageItem(
                        item.id(),
                        item.sessionId(),
                        item.title(),
                        item.preview(),
                        TIME_FORMATTER.format(item.createdAt())
                ))
                .toList();

        log.info("Dashboard summary loaded: knowledgeBaseCount={}, sessionCount={}, recentMessageCount={}",
                knowledgeBaseCount,
                sessionCount,
                recentMessages.size());
        return new DashboardSummaryResponse(knowledgeBaseCount, sessionCount, recentMessages);
    }

    private String buildPreview(String content) {
        if (content == null || content.isBlank()) {
            return "-";
        }
        String normalized = content.strip();
        return normalized.length() <= 60 ? normalized : normalized.substring(0, 60);
    }

    private String titleOrFallback(String title) {
        if (title == null || title.isBlank()) {
            return "未命名会话";
        }
        return title.strip();
    }

    private record RecentMessageCandidate(
            String id,
            String sessionId,
            String title,
            String preview,
            Instant createdAt
    ) {
    }
}
