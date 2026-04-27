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
 * 知识文档实体。
 * 记录知识库中文档的文件信息、导入状态和切片数量，作为后续向量化处理的元数据来源。
 */
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

    /**
     * 文档首次入库时初始化标识、时间和默认切片数量。
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
        if (chunkCount == null) {
            chunkCount = 0;
        }
    }

    /**
     * 文档元数据变化时同步刷新更新时间。
     */
    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }
}
