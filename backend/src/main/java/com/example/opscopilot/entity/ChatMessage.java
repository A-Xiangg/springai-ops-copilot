package com.example.opscopilot.entity;

import com.example.opscopilot.support.IdGenerator;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

/**
 * 聊天消息实体。
 * 按会话维度存储用户提问和助手回答，是后续实现上下文追溯和审计的基础数据。
 */
@Getter
@Setter
@Entity
@Table(name = "chat_message")
public class ChatMessage {

    @Id
    private Long id;

    @Column(nullable = false)
    private Long sessionId;

    @Column(nullable = false, length = 32)
    private String role;

    @Column(nullable = false, columnDefinition = "text")
    private String content;

    @Column(length = 64)
    private String modelName;

    private Integer tokenUsage;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * 首次持久化消息时生成主键并补齐创建时间。
     */
    @PrePersist
    void prePersist() {
        if (id == null) {
            id = IdGenerator.nextId();
        }
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
