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
 * 知识库实体。
 * 表示一个独立的知识集合，用于组织文档、权限归属以及启停状态。
 */
@Getter
@Setter
@Entity
@Table(name = "kb_knowledge_base")
public class KnowledgeBase {

    @Id
    private Long id;

    @Column(nullable = false, length = 128)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private Short status = 1;

    private Long createdBy;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    /**
     * 创建知识库时补齐主键、时间和默认状态。
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
     * 知识库元数据更新时刷新修改时间。
     */
    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }
}
