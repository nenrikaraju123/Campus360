package com.campus360.platform.security;

import com.campus360.platform.tenancy.TenantContext;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * Validates the Bearer JWT on each request, populates the SecurityContext and
 * the {@link TenantContext}. Stateless — no server session.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                Claims claims = jwtService.parse(token);
                String userId = claims.getSubject();
                Number tenant = claims.get("tenantId", Number.class);
                List<String> roles = claims.get("roles", List.class);

                Collection<SimpleGrantedAuthority> authorities = roles == null ? List.of()
                        : roles.stream().map(r -> new SimpleGrantedAuthority("ROLE_" + r)).toList();

                AuthPrincipal principal = new AuthPrincipal(
                        Long.valueOf(userId),
                        tenant == null ? null : tenant.longValue(),
                        claims.get("email", String.class));

                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(principal, null, authorities);
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
                TenantContext.setTenantId(principal.tenantId());
            } catch (Exception ignored) {
                // Invalid token -> leave context unauthenticated; entry point handles 401.
            }
        } else {
            // Support for MockMvc tests: if SecurityContext is already populated by @WithMockCampusUser,
            // ensure TenantContext is populated for this request before proceeding.
            if (SecurityContextHolder.getContext().getAuthentication() != null &&
                SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof AuthPrincipal p) {
                TenantContext.setTenantId(p.tenantId());
            }
        }

        try {
            chain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }

    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        return false;
    }

    @Override
    protected boolean shouldNotFilterErrorDispatch() {
        return false;
    }

    /** Lightweight authenticated principal stored in the SecurityContext. */
    public record AuthPrincipal(Long userId, Long tenantId, String email) {
    }
}
