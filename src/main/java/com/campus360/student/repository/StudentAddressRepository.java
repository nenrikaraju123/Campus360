package com.campus360.student.repository;

import com.campus360.student.domain.StudentAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentAddressRepository extends JpaRepository<StudentAddress, Long> {
    List<StudentAddress> findByTenantIdAndStudent_Id(Long tenantId, Long studentId);
}
