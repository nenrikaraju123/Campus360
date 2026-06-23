package com.campus360.student.repository;

import com.campus360.student.domain.StudentTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentTagRepository extends JpaRepository<StudentTag, Long> {
    Optional<StudentTag> findByTenantIdAndName(Long tenantId, String name);
}
