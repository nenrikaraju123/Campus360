package com.campus360.student.repository;

import com.campus360.student.domain.StudentLifecycleHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StudentLifecycleHistoryRepository extends JpaRepository<StudentLifecycleHistory, Long> {
    List<StudentLifecycleHistory> findByStudentIdOrderByCreatedAtDesc(Long studentId);
}
