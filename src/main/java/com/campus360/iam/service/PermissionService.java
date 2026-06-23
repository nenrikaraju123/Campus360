package com.campus360.iam.service;

import com.campus360.iam.domain.Permission;
import com.campus360.iam.domain.Role;
import com.campus360.iam.repository.PermissionRepository;
import com.campus360.iam.repository.RoleRepository;
import com.campus360.platform.error.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class PermissionService {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;

    public PermissionService(PermissionRepository permissionRepository, RoleRepository roleRepository) {
        this.permissionRepository = permissionRepository;
        this.roleRepository = roleRepository;
    }

    @Transactional(readOnly = true)
    public List<Permission> getAllPermissions() {
        return permissionRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Set<Permission> getRolePermissions(Long roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> ApiException.notFound("Role not found: " + roleId));
        return role.getPermissions();
    }

    public void updateRolePermissions(Long roleId, Set<String> permissionNames) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> ApiException.notFound("Role not found: " + roleId));

        Set<Permission> newPermissions = permissionNames.stream()
                .map(name -> permissionRepository.findByName(name)
                        .orElseThrow(() -> ApiException.badRequest("Unknown permission: " + name)))
                .collect(Collectors.toSet());

        role.setPermissions(newPermissions);
        roleRepository.save(role);
    }
}
