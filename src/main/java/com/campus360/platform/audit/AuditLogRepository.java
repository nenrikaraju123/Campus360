package com.campus360.platform.audit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AuditLogRepository extends JpaRepository<AuditLogEntry, Long>, JpaSpecificationExecutor<AuditLogEntry> {

    Page<AuditLogEntry> findByTenantIdOrderByCreatedAtDesc(Long tenantId, Pageable pageable);

    Page<AuditLogEntry> findByTenantIdAndEntityTypeOrderByCreatedAtDesc(Long tenantId, String entityType, Pageable pageable);

    Page<AuditLogEntry> findByTenantIdAndActorIdOrderByCreatedAtDesc(Long tenantId, Long actorId, Pageable pageable);

    Page<AuditLogEntry> findByOrderByCreatedAtDesc(Pageable pageable);
}
