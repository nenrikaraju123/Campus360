package com.campus360.institution.repository;

import com.campus360.institution.domain.Institution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InstitutionRepository extends JpaRepository<Institution, Long> {

    boolean existsByCodeIgnoreCase(String code);

    Optional<Institution> findByCodeIgnoreCase(String code);
}
