package com.campus360.platform.security;

import com.campus360.platform.security.JwtAuthFilter.AuthPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/** Convenience accessors for the authenticated principal. */
public final class CurrentUser {

    private CurrentUser() {
    }

    public static AuthPrincipal principal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AuthPrincipal ap) {
            return ap;
        }
        return null;
    }

    public static Long id() {
        AuthPrincipal p = principal();
        return p == null ? null : p.userId();
    }

    public static Long tenantId() {
        AuthPrincipal p = principal();
        return p == null ? null : p.tenantId();
    }
}
