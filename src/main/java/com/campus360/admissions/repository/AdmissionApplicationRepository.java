package com.campus360.admissions.repository;

import com.campus360.admissions.domain.AdmissionApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface AdmissionApplicationRepository
        extends JpaRepository<AdmissionApplication, Long>, JpaSpecificationExecutor<AdmissionApplication> {

    Page<AdmissionApplication> findByTenantId(Long tenantId, Pageable pageable);
    Page<AdmissionApplication> findByTenantIdAndStatus(Long tenantId, String status, Pageable pageable);
    Optional<AdmissionApplication> findByIdAndTenantId(Long id, Long tenantId);
    boolean existsByTenantIdAndEmail(Long tenantId, String email);
    boolean existsByTenantIdAndApplicationNumber(Long tenantId, String applicationNumber);
}
