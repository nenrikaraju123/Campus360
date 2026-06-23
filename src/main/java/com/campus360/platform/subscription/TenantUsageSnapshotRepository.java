package com.campus360.platform.subscription;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface TenantUsageSnapshotRepository extends JpaRepository<TenantUsageSnapshot, Long> {
    Optional<TenantUsageSnapshot> findByTenantIdAndSnapshotDate(Long tenantId, LocalDate snapshotDate);
}
