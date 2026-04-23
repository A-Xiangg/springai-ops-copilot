package com.example.opscopilot.repository;

import com.example.opscopilot.entity.Conversation;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 对话记录数据访问仓储。
 *
 * @author ops-copilot
 * @date 2026/04/21
 */
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    List<Conversation> findTop20BySessionIdOrderByCreatedAtDesc(String sessionId);
}
