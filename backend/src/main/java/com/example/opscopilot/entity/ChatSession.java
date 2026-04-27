package com.example.opscopilot.entity;

import com.example.opscopilot.support.IdGenerator;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

/**
 * 聊天会话实体。
 * 一条记录对应前端看到的一个对话窗口，负责承载会话标题、归属用户和最近更新时间。
 */
@Getter
@Setter
@Entity
@Table(name = "chat_session")
public class ChatSession {

    @Id
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(length = 255)
    private String title;

    @Column(nullable = false)
    private Short status = 1;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    /**
     * 首次入库前补齐主键、时间戳和默认状态，保证业务层不必重复处理这些通用字段。
     */
    @PrePersist
    void prePersist() {
        if (id == null) {
            id = IdGenerator.nextId();
        }
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
        if (status == null) {
            status = 1;
        }
    }

    /**
     * 每次更新会话时刷新最后修改时间，用于支撑会话列表按最近活跃排序。
     */
    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }
}
