package com.campus360.student.repository;

import com.campus360.student.domain.StudentDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentDocumentRepository extends JpaRepository<StudentDocument, Long> {
    List<StudentDocument> findByTenantIdAndStudent_Id(Long tenantId, Long studentId);
}
