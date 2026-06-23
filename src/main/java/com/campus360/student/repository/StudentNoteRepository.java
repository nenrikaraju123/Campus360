package com.campus360.student.repository;

import com.campus360.student.domain.StudentNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentNoteRepository extends JpaRepository<StudentNote, Long> {
    List<StudentNote> findByTenantIdAndStudent_Id(Long tenantId, Long studentId);
}
