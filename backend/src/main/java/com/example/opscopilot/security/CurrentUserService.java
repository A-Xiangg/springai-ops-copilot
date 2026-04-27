package com.example.opscopilot.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

/**
 * 当前登录用户访问器。
 */
@Service
public class CurrentUserService {

    /**
     * 获取当前已认证用户。
     */
    public AuthenticatedUser requireCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(UNAUTHORIZED, "未登录或登录已失效");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof AuthenticatedUser authenticatedUser) {
            return authenticatedUser;
        }
        throw new ResponseStatusException(UNAUTHORIZED, "未登录或登录已失效");
    }

    /**
     * 获取当前用户 id。
     */
    public Long requireCurrentUserId() {
        return requireCurrentUser().userId();
    }
}
