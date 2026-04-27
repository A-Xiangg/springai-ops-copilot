package com.example.opscopilot.dto;

import java.util.List;

/**
 * 首页概览响应。
 */
public record DashboardSummaryResponse(
        long knowledgeBaseCount,
        long sessionCount,
        List<RecentMessageItem> recentMessages
) {

    /**
     * 首页最近消息摘要。
     */
    public record RecentMessageItem(
            String id,
            String sessionId,
            String title,
            String preview,
            String time
    ) {
    }
}
