package com.example.opscopilot.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 文档配置。
 *
 * @author ops-copilot
 * @date 2026/04/21
 */
@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI opsCopilotOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Spring AI Ops Copilot API")
                        .version("0.0.1")
                        .description("Operations copilot APIs powered by Spring AI."));
    }
}
