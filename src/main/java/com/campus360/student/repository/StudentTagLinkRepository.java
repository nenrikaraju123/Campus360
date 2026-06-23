package com.campus360.student.repository;

import com.campus360.student.domain.StudentTagLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentTagLinkRepository extends JpaRepository<StudentTagLink, Long> {
    List<StudentTagLink> findByTenantIdAndStudent_Id(Long tenantId, Long studentId);
}
