package com.campus360.academics.repository;

import com.campus360.academics.domain.Enrollment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    Optional<Enrollment> findByIdAndTenantId(Long id, Long tenantId);

    Page<Enrollment> findByTenantIdAndTermId(Long tenantId, Long termId, Pageable pageable);

    List<Enrollment> findByTenantIdAndStudentId(Long tenantId, Long studentId);

    List<Enrollment> findByTenantIdAndStudentIdAndTermId(Long tenantId, Long studentId, Long termId);

    List<Enrollment> findByTenantIdAndSectionId(Long tenantId, Long sectionId);

    boolean existsByStudentIdAndSectionId(Long studentId, Long sectionId);

    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.sectionId = :sectionId AND e.status = 'ENROLLED'")
    long countActiveBySectionId(@Param("sectionId") Long sectionId);

    @Query("SELECT e FROM Enrollment e WHERE e.tenantId = :tenantId AND e.studentId = :studentId AND e.status = 'ENROLLED'")
    List<Enrollment> findActiveByStudent(@Param("tenantId") Long tenantId, @Param("studentId") Long studentId);
}
