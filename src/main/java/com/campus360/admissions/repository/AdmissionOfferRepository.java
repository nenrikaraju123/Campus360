package com.campus360.admissions.repository;

import com.campus360.admissions.domain.AdmissionOffer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AdmissionOfferRepository extends JpaRepository<AdmissionOffer, Long> {
    Optional<AdmissionOffer> findByIdAndTenantId(Long id, Long tenantId);
    Optional<AdmissionOffer> findByApplicationId(Long applicationId);
}
