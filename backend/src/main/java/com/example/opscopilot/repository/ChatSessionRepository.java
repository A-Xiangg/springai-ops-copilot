package com.example.opscopilot.repository;

import com.example.opscopilot.entity.ChatSession;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {

    List<ChatSession> findByUserIdAndStatusOrderByUpdatedAtDesc(Long userId, Short status);
}
