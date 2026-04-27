package com.example.opscopilot.controller;

import com.example.opscopilot.dto.CurrentUserResponse;
import com.example.opscopilot.dto.LoginRequest;
import com.example.opscopilot.dto.LoginResponse;
import com.example.opscopilot.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * 认证接口控制器。
 */
@RestController
@RequestMapping("/api/auth")
@Slf4j
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 最小登录接口，供前端骨架联调。
     */
    @PostMapping("/login")
    @Operation(summary = "Login with username and password")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        log.info("Received login request: username={}", request.username());
        return authService.login(request);
    }

    /**
     * 返回当前登录用户信息。
     */
    @GetMapping("/me")
    @Operation(summary = "Get current user")
    public CurrentUserResponse currentUser() {
        log.info("Received current user request");
        return authService.currentUser();
    }

    /**
     * 注销当前登录 token。
     */
    @PostMapping("/logout")
    @Operation(summary = "Logout current user")
    public void logout(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader) {
        log.info("Received logout request");
        authService.logout(authorizationHeader);
    }
}
