package com.example.opscopilot.repository;

import com.example.opscopilot.entity.KnowledgeBase;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KnowledgeBaseRepository extends JpaRepository<KnowledgeBase, Long> {

    List<KnowledgeBase> findByStatusOrderByCreatedAtDesc(Short status);
}
