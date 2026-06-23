package com.campus360.iam.repository;

import com.campus360.iam.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Tenant-scoped lookups: email is unique per tenant, not globally.
    Optional<User> findByTenantIdAndEmailIgnoreCase(Long tenantId, String email);

    boolean existsByTenantIdAndEmailIgnoreCase(Long tenantId, String email);

    // Platform accounts (SUPER_ADMIN) live with a null tenant.
    Optional<User> findByTenantIdIsNullAndEmailIgnoreCase(String email);

    boolean existsByTenantIdIsNullAndEmailIgnoreCase(String email);

    // ---- Paginated tenant-scoped queries ----
    Page<User> findByTenantId(Long tenantId, Pageable pageable);

    Page<User> findByTenantIdAndRolesNameIgnoreCase(Long tenantId, String roleName, Pageable pageable);

    Optional<User> findByIdAndTenantId(Long id, Long tenantId);

    // ---- Tenant stats queries ----
    long countByTenantId(Long tenantId);

    @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE u.tenantId = :tenantId AND UPPER(r.name) = UPPER(:roleName)")
    long countByTenantIdAndRoleName(@Param("tenantId") Long tenantId, @Param("roleName") String roleName);

    /** Find all users with a given role name across the platform (used to notify super admins). */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE UPPER(r.name) = UPPER(:roleName) AND u.tenantId IS NULL")
    List<User> findPlatformUsersByRole(@Param("roleName") String roleName);
}

