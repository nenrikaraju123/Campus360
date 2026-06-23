package com.campus360.admissions.repository;

import com.campus360.admissions.domain.AdmissionStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AdmissionStatusHistoryRepository extends JpaRepository<AdmissionStatusHistory, Long> {
    List<AdmissionStatusHistory> findByApplicationIdOrderByCreatedAtDesc(Long applicationId);
}
