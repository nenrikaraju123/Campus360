package com.campus360.admissions.repository;

import com.campus360.admissions.domain.AdmissionNote;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AdmissionNoteRepository extends JpaRepository<AdmissionNote, Long> {
    List<AdmissionNote> findByApplicationIdOrderByCreatedAtDesc(Long applicationId);
}
