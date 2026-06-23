package com.campus360.finance.service;

import com.campus360.finance.domain.FeeConcession;
import com.campus360.finance.domain.FinanceStatusHistory;
import com.campus360.finance.repository.FeeConcessionRepository;
import com.campus360.finance.repository.FinanceStatusHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class ConcessionService {

    private final FeeConcessionRepository concessionRepository;
    private final FinanceStatusHistoryRepository historyRepository;

    @Transactional
    public FeeConcession requestConcession(FeeConcession concession) {
        concession.setStatus("PENDING");
        FeeConcession saved = concessionRepository.save(concession);
        recordHistory(saved, null, "PENDING", "SYSTEM", "Concession requested");
        return saved;
    }

    @Transactional
    public FeeConcession approveConcession(Long tenantId, Long concessionId, String actorId) {
        FeeConcession concession = concessionRepository.findById(concessionId)
                .orElseThrow(() -> new IllegalArgumentException("Concession not found"));

        if (!concession.getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException("Tenant mismatch");
        }

        String oldStatus = concession.getStatus();
        concession.setStatus("APPROVED");
        concession.setApprovedBy(actorId);
        concession.setApprovedAt(Instant.now());

        FeeConcession saved = concessionRepository.save(concession);
        recordHistory(saved, oldStatus, "APPROVED", actorId, "Concession approved");
        return saved;
    }

    private void recordHistory(FeeConcession concession, String oldStatus, String newStatus, String actor, String comments) {
        FinanceStatusHistory history = new FinanceStatusHistory();
        history.setTenantId(concession.getTenantId());
        history.setEntityType("CONCESSION");
        history.setEntityId(concession.getId());
        history.setPreviousStatus(oldStatus);
        history.setNewStatus(newStatus);
        history.setChangedBy(actor);
        history.setComments(comments);
        historyRepository.save(history);
    }
}
