package com.campus360.student.repository;

import com.campus360.student.domain.StudentGuardian;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface StudentGuardianRepository extends JpaRepository<StudentGuardian, Long> {
    List<StudentGuardian> findByStudentIdOrderByIsPrimaryDesc(Long studentId);
    Optional<StudentGuardian> findByIdAndTenantId(Long id, Long tenantId);
}
