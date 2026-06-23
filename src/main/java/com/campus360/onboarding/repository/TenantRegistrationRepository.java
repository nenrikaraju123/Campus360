package com.campus360.onboarding.repository;

import com.campus360.onboarding.domain.RegistrationStatus;
import com.campus360.onboarding.domain.TenantRegistration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TenantRegistrationRepository extends JpaRepository<TenantRegistration, Long> {

    List<TenantRegistration> findByStatusOrderByCreatedAtDesc(RegistrationStatus status);

    List<TenantRegistration> findAllByOrderByCreatedAtDesc();

    boolean existsByInstitutionCodeIgnoreCaseAndStatus(String institutionCode, RegistrationStatus status);

    boolean existsByAdminEmailIgnoreCaseAndStatus(String adminEmail, RegistrationStatus status);
}
