package com.campus360.placement.repository;

import com.campus360.placement.domain.Application;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    List<Application> findByTenantIdAndPostingId(Long tenantId, Long postingId);

    List<Application> findByTenantIdAndStudentId(Long tenantId, Long studentId);

    Optional<Application> findByPostingIdAndStudentId(Long postingId, Long studentId);

    Optional<Application> findByIdAndTenantId(Long id, Long tenantId);

    boolean existsByPostingIdAndStudentId(Long postingId, Long studentId);
}
