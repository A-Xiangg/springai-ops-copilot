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

@Getter
@Setter
@Entity
@Table(name = "kb_document")
public class KnowledgeDocument {

    @Id
    private Long id;

    @Column(nullable = false)
    private Long knowledgeBaseId;

    @Column(nullable = false, length = 255)
    private String fileName;

    @Column(length = 32)
    private String fileType;

    private Long fileSize;

    @Column(nullable = false, length = 32)
    private String status;

    @Column(length = 500)
    private String originalPath;

    private Integer chunkCount = 0;

    private Long createdBy;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

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
        if (chunkCount == null) {
            chunkCount = 0;
        }
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }
}
