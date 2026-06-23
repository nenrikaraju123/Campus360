package com.campus360.placement.repository;

import com.campus360.placement.domain.CareerProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CareerProfileRepository extends JpaRepository<CareerProfile, Long> {
    Optional<CareerProfile> findByTenantIdAndStudentId(Long tenantId, Long studentId);
}
