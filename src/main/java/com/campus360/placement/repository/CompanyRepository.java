package com.campus360.placement.repository;

import com.campus360.placement.domain.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {

    List<Company> findByTenantId(Long tenantId);

    Optional<Company> findByIdAndTenantId(Long id, Long tenantId);
}
