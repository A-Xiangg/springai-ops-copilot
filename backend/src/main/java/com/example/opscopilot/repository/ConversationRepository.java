package com.example.opscopilot.repository;

import com.example.opscopilot.entity.Conversation;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    List<Conversation> findTop20BySessionIdOrderByCreatedAtDesc(String sessionId);
}
