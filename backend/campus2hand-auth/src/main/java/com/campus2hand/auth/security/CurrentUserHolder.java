package com.campus2hand.auth.security;

import com.campus2hand.common.constant.ErrorCode;
import com.campus2hand.common.exception.BusinessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class CurrentUserHolder {

    private static final ThreadLocal<Long> USER_ID_HOLDER = new ThreadLocal<>();

    private CurrentUserHolder() {
    }

    public static void setUserId(Long userId) {
        USER_ID_HOLDER.set(userId);
    }

    public static void clear() {
        USER_ID_HOLDER.remove();
    }

    public static Long getUserId() {
        Long userId = USER_ID_HOLDER.get();
        if (userId != null) {
            return userId;
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Long) {
            return (Long) authentication.getPrincipal();
        }
        throw new BusinessException(ErrorCode.TOKEN_INVALID, "未登录");
    }

    public static String getRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getAuthorities() != null
                && !authentication.getAuthorities().isEmpty()) {
            String authority = authentication.getAuthorities().iterator().next().getAuthority();
            if (authority.startsWith("ROLE_")) {
                return authority.substring(5);
            }
            return authority;
        }
        throw new BusinessException(ErrorCode.TOKEN_INVALID, "未登录");
    }

    public static boolean isAdmin() {
        return "admin".equals(getRole());
    }

    public static void requireAdmin() {
        if (!isAdmin()) {
            throw new BusinessException(ErrorCode.ADMIN_PERMISSION_DENIED, "需要管理员权限");
        }
    }

    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof Long;
    }
}