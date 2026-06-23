package com.campus360.admissions.repository;

import com.campus360.admissions.domain.AdmissionDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdmissionDocumentRepository extends JpaRepository<AdmissionDocument, Long> {
    List<AdmissionDocument> findByTenantIdAndApplication_Id(Long tenantId, Long applicationId);
}
