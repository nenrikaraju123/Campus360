package com.campus360.admissions.repository;

import com.campus360.admissions.domain.AdmissionLead;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdmissionLeadRepository extends JpaRepository<AdmissionLead, Long> {
    Page<AdmissionLead> findByTenantId(Long tenantId, Pageable pageable);
    Page<AdmissionLead> findByTenantIdAndStatus(Long tenantId, String status, Pageable pageable);
    Optional<AdmissionLead> findByIdAndTenantId(Long id, Long tenantId);
    boolean existsByTenantIdAndEmail(Long tenantId, String email);
}
