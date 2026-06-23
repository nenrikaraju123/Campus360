package com.campus360.student.repository;

import com.campus360.student.domain.ParentStudentLink;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ParentStudentLinkRepository extends JpaRepository<ParentStudentLink, Long> {
    List<ParentStudentLink> findByParentIdAndIsActiveTrue(Long parentId);
    boolean existsByTenantIdAndParentIdAndStudentId(Long tenantId, Long parentId, Long studentId);
}
