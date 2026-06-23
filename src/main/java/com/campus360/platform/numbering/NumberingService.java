package com.campus360.platform.numbering;

import com.campus360.platform.tenancy.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
public class NumberingService {

    private final NumberSequenceRepository repository;

    public NumberingService(NumberSequenceRepository repository) {
        this.repository = repository;
    }

    /**
     * Generates the next number for a given sequence key in a separate transaction.
     * This prevents sequence generation from causing large transaction rollbacks
     * and blocking other transactions for too long.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String generateNextNumber(String sequenceKey, String financialYear) {
        Long tenantId = TenantContext.requireTenantId();
        
        Optional<NumberSequence> optSeq;
        if (financialYear != null && !financialYear.isBlank()) {
            optSeq = repository.findAndLockByTenantIdAndSequenceKeyAndFinancialYear(tenantId, sequenceKey, financialYear);
        } else {
            optSeq = repository.findAndLockByTenantIdAndSequenceKeyWithoutYear(tenantId, sequenceKey);
        }

        NumberSequence sequence = optSeq.orElseGet(() -> {
            NumberSequence newSeq = new NumberSequence();
            newSeq.setTenantId(tenantId);
            newSeq.setSequenceKey(sequenceKey);
            newSeq.setFinancialYear(financialYear);
            newSeq.setPrefix(sequenceKey.substring(0, Math.min(3, sequenceKey.length())).toUpperCase() + "-");
            newSeq.setPadding(4);
            newSeq.setNextValue(1L);
            return newSeq;
        });

        Long currentValue = sequence.getNextValue();
        sequence.setNextValue(currentValue + 1);
        sequence.setUpdatedAt(Instant.now());
        
        repository.save(sequence);

        return formatNumber(sequence.getPrefix(), sequence.getFinancialYear(), currentValue, sequence.getPadding());
    }

    public String generateNextNumber(String sequenceKey) {
        return generateNextNumber(sequenceKey, null);
    }

    private String formatNumber(String prefix, String financialYear, Long value, int padding) {
        StringBuilder sb = new StringBuilder();
        if (prefix != null) {
            sb.append(prefix);
        }
        if (financialYear != null) {
            sb.append(financialYear).append("-");
        }
        
        String numberStr = String.valueOf(value);
        if (padding > 0 && numberStr.length() < padding) {
            sb.append("0".repeat(padding - numberStr.length()));
        }
        sb.append(numberStr);
        
        return sb.toString();
    }
}
