package com.campus360.testsupport;

import com.campus360.iam.domain.User;
import com.campus360.iam.domain.UserStatus;
import com.campus360.iam.repository.RoleRepository;
import com.campus360.iam.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

@Component
public class TestUserFactory {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    public User createUser(Long tenantId, String roleName) {
        User user = new User();
        user.setTenantId(tenantId);
        user.setEmail(UUID.randomUUID().toString() + "@test.com");
        user.setFullName("Test User " + roleName);
        user.setPasswordHash("hashed-password");
        user.setStatus(UserStatus.ACTIVE);
        user.setMustChangePassword(false);

        com.campus360.iam.domain.Role role = roleRepository.findByName(roleName).orElseGet(() -> {
            com.campus360.iam.domain.Role newRole = new com.campus360.iam.domain.Role();
            newRole.setName(roleName);
            return roleRepository.save(newRole);
        });
        
        user.setRoles(Set.of(role));
        return userRepository.save(user);
    }
}
