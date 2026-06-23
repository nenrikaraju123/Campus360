package com.campus360.testsupport;

import com.campus360.platform.security.JwtAuthFilter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.util.List;

public class WithMockCampusUserSecurityContextFactory implements WithSecurityContextFactory<WithMockCampusUser> {

    @Override
    public SecurityContext createSecurityContext(WithMockCampusUser annotation) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        
        JwtAuthFilter.AuthPrincipal principal = new JwtAuthFilter.AuthPrincipal(
                annotation.userId(),
                annotation.tenantId() == -1 ? null : annotation.tenantId(),
                annotation.email()
        );

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(principal, null, List.of(new SimpleGrantedAuthority("ROLE_" + annotation.role())));
        
        context.setAuthentication(auth);
        com.campus360.platform.tenancy.TenantContext.setTenantId(principal.tenantId());
        return context;
    }
}
