package com.campus360.placement.repository;

import com.campus360.placement.domain.JobPosting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JobPostingRepository extends JpaRepository<JobPosting, Long> {

    List<JobPosting> findByTenantId(Long tenantId);

    List<JobPosting> findByTenantIdAndStatus(Long tenantId, String status);

    Optional<JobPosting> findByIdAndTenantId(Long id, Long tenantId);
}
