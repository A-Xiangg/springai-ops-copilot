package com.example.opscopilot.security;

import com.example.opscopilot.entity.SysUser;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 基于内存的登录令牌服务。
 */
@Service
@Slf4j
public class AuthTokenService {

    private final Map<String, AuthenticatedUser> tokenStore = new ConcurrentHashMap<>();

    /**
     * 为登录成功的用户创建 token。
     */
    public AuthenticatedUser issueToken(SysUser user) {
        String token = UUID.randomUUID().toString().replace("-", "");
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                Instant.now(),
                token
        );
        tokenStore.put(token, authenticatedUser);
        log.info("Issued auth token: userId={}, username={}", user.getId(), user.getUsername());
        return authenticatedUser;
    }

    /**
     * 根据 token 查询当前登录用户。
     */
    public Optional<AuthenticatedUser> resolve(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(tokenStore.get(token.trim()));
    }

    /**
     * 注销 token。
     */
    public void revoke(String token) {
        if (token == null || token.isBlank()) {
            return;
        }
        AuthenticatedUser removed = tokenStore.remove(token.trim());
        if (removed != null) {
            log.info("Revoked auth token: userId={}, username={}", removed.userId(), removed.username());
        }
    }
}
