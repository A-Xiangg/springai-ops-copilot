package com.example.opscopilot.controller;

import com.example.opscopilot.dto.DashboardSummaryResponse;
import com.example.opscopilot.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 首页概览接口控制器。
 */
@RestController
@RequestMapping("/api/dashboard")
@Slf4j
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * 返回首页统计信息。
     */
    @GetMapping("/summary")
    @Operation(summary = "Get dashboard summary")
    public DashboardSummaryResponse getSummary() {
        log.info("Received dashboard summary request");
        return dashboardService.getSummary();
    }
}
