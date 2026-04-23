package com.example.opscopilot.repository;

import com.example.opscopilot.entity.KnowledgeDocument;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KnowledgeDocumentRepository extends JpaRepository<KnowledgeDocument, Long> {

    List<KnowledgeDocument> findByKnowledgeBaseIdOrderByCreatedAtDesc(Long knowledgeBaseId);
}
