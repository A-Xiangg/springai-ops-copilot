package com.example.opscopilot.repository;

import com.example.opscopilot.entity.AiModelConfig;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiModelConfigRepository extends JpaRepository<AiModelConfig, Long> {

    Optional<AiModelConfig> findByModelCode(String modelCode);

    List<AiModelConfig> findByEnabledOrderByCreatedAtDesc(Short enabled);
}
