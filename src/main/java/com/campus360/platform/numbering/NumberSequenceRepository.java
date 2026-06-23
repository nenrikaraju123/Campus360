package com.campus360.platform.numbering;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;

public interface NumberSequenceRepository extends JpaRepository<NumberSequence, Long> {
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT n FROM NumberSequence n WHERE n.tenantId = :tenantId AND n.sequenceKey = :sequenceKey AND n.financialYear = :financialYear")
    Optional<NumberSequence> findAndLockByTenantIdAndSequenceKeyAndFinancialYear(Long tenantId, String sequenceKey, String financialYear);
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT n FROM NumberSequence n WHERE n.tenantId = :tenantId AND n.sequenceKey = :sequenceKey AND n.financialYear IS NULL")
    Optional<NumberSequence> findAndLockByTenantIdAndSequenceKeyWithoutYear(Long tenantId, String sequenceKey);
}
