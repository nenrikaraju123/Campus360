package com.campus360.platform.bootstrap;

import com.campus360.iam.domain.RoleName;
import com.campus360.iam.domain.User;
import com.campus360.iam.domain.UserStatus;
import com.campus360.iam.repository.RoleRepository;
import com.campus360.iam.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Ensures a platform SUPER_ADMIN exists so the system is operable on first boot.
 * Credentials come from config; the default password must be rotated in any real
 * environment (a warning is logged when the default is used).
 */
@Component
public class PlatformBootstrap implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(PlatformBootstrap.class);

    private final UserRepository users;
    private final RoleRepository roles;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminPassword;

    public PlatformBootstrap(UserRepository users, RoleRepository roles, PasswordEncoder passwordEncoder,
                             @Value("${campus360.bootstrap.super-admin-email:admin@campus360.local}") String adminEmail,
                             @Value("${campus360.bootstrap.super-admin-password:ChangeMe!123}") String adminPassword) {
        this.users = users;
        this.roles = roles;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(String... args) {
        if (users.existsByTenantIdIsNullAndEmailIgnoreCase(adminEmail)) {
            return;
        }
        User admin = new User();
        admin.setEmail(adminEmail);
        admin.setFullName("Platform Super Admin");
        admin.setPasswordHash(passwordEncoder.encode(adminPassword));
        admin.setStatus(UserStatus.ACTIVE);
        admin.setTenantId(null);
        roles.findByName(RoleName.SUPER_ADMIN.name()).ifPresent(r -> admin.setRoles(Set.of(r)));
        users.save(admin);

        log.info("Seeded platform SUPER_ADMIN: {}", adminEmail);
        if ("ChangeMe!123".equals(adminPassword)) {
            log.warn("SUPER_ADMIN is using the DEFAULT password. Set campus360.bootstrap.super-admin-password now.");
        }
    }
}
