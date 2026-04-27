package com.example.opscopilot.service;

import com.example.opscopilot.dto.CurrentUserResponse;
import com.example.opscopilot.dto.LoginRequest;
import com.example.opscopilot.dto.LoginResponse;
import com.example.opscopilot.entity.SysUser;
import com.example.opscopilot.repository.SysUserRepository;
import com.example.opscopilot.security.AuthTokenService;
import com.example.opscopilot.security.AuthenticatedUser;
import com.example.opscopilot.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

/**
 * 登录业务服务。
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final AuthTokenService authTokenService;
    private final CurrentUserService currentUserService;
    private final SysUserRepository sysUserRepository;

    /**
     * 基于现有用户表完成最小登录校验。
     */
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        SysUser user = sysUserRepository.findByUsername(request.username().trim())
                .filter(item -> item.getStatus() != null && item.getStatus() == 1)
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "用户名或密码错误"));

        if (!user.getPassword().equals(request.password())) {
            log.warn("Login failed because password does not match: username={}", request.username());
            throw new ResponseStatusException(UNAUTHORIZED, "用户名或密码错误");
        }

        AuthenticatedUser authenticatedUser = authTokenService.issueToken(user);
        log.info("Login succeeded: userId={}, username={}", user.getId(), user.getUsername());
        return new LoginResponse(
                authenticatedUser.userId(),
                authenticatedUser.username(),
                authenticatedUser.nickname(),
                authenticatedUser.token(),
                authenticatedUser.loginAt()
        );
    }

    /**
     * 获取当前登录用户。
     */
    @Transactional(readOnly = true)
    public CurrentUserResponse currentUser() {
        AuthenticatedUser authenticatedUser = currentUserService.requireCurrentUser();
        return new CurrentUserResponse(
                authenticatedUser.userId(),
                authenticatedUser.username(),
                authenticatedUser.nickname(),
                authenticatedUser.loginAt()
        );
    }

    /**
     * 注销当前登录 token。
     */
    public void logout(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return;
        }
        authTokenService.revoke(authorizationHeader.substring(7).trim());
    }
}
