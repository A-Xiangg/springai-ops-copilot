package com.example.opscopilot.controller;

import com.example.opscopilot.dto.ChatRequest;
import com.example.opscopilot.dto.ChatResponse;
import com.example.opscopilot.dto.ChatMessageSearchResponse;
import com.example.opscopilot.dto.ChatSessionDetailResponse;
import com.example.opscopilot.dto.ChatSessionSummaryResponse;
import com.example.opscopilot.service.ChatQueryService;
import com.example.opscopilot.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 对话接口控制器，负责接收用户消息并返回运维 Copilot 回复。
 *
 * @author ops-copilot
 * @date 2026/04/21
 */
@RestController
@RequestMapping("/api/chat")
@Slf4j
@RequiredArgsConstructor
public class ChatController {

    private final ChatQueryService chatQueryService;
    private final ChatService chatService;

    /**
     * 对外暴露聊天入口。
     * 控制器本身只做参数接收和校验，具体会话编排与模型调用交给 ChatService。
     */
    @PostMapping
    @Operation(summary = "Send a message to the operations copilot")
    public ChatResponse chat(@Valid @RequestBody ChatRequest request) {
        log.info("Received chat request: sessionId={}, messageLength={}",
                request.sessionId(),
                request.message().length());
        return chatService.chat(request);
    }

    /**
     * 获取会话列表。
     */
    @GetMapping("/sessions")
    @Operation(summary = "List chat sessions")
    public List<ChatSessionSummaryResponse> listSessions() {
        log.info("Received chat session list request");
        return chatQueryService.listSessions();
    }

    /**
     * 获取单个会话的历史消息。
     */
    @GetMapping("/sessions/{sessionId}/messages")
    @Operation(summary = "Get chat session messages")
    public ChatSessionDetailResponse getSessionMessages(@PathVariable String sessionId) {
        log.info("Received chat session detail request: sessionId={}", sessionId);
        return chatQueryService.getSessionDetail(sessionId);
    }

    /**
     * 搜索历史聊天内容。
     */
    @GetMapping("/messages/search")
    @Operation(summary = "Search chat messages")
    public List<ChatMessageSearchResponse> searchMessages(@RequestParam String keyword) {
        log.info("Received chat message search request: keywordLength={}", keyword == null ? 0 : keyword.length());
        return chatQueryService.searchMessages(keyword);
    }
}
