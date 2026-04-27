package com.example.opscopilot.repository;

import com.example.opscopilot.entity.ChatMessage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findBySessionIdOrderByCreatedAtAsc(Long sessionId);

    @Query("""
            select m
            from ChatMessage m
            where m.sessionId in (
                select s.id
                from ChatSession s
                where s.userId = :userId and s.status = 1
            )
            and lower(m.content) like lower(concat('%', :keyword, '%'))
            order by m.createdAt desc
            """)
    List<ChatMessage> searchByUserIdAndContent(@Param("userId") Long userId,
                                               @Param("keyword") String keyword,
                                               Pageable pageable);
}
