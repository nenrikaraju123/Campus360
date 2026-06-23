package com.campus360.placement.repository;

import com.campus360.placement.domain.Offer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OfferRepository extends JpaRepository<Offer, Long> {

    List<Offer> findByTenantId(Long tenantId);

    Optional<Offer> findByIdAndTenantId(Long id, Long tenantId);

    List<Offer> findByTenantIdAndApplicationId(Long tenantId, Long applicationId);

    List<Offer> findByTenantIdAndApplicationIdIn(Long tenantId, List<Long> applicationIds);
}
