package com.campus360.finance.service;

import com.campus360.finance.domain.Refund;
import com.campus360.finance.domain.FinanceStatusHistory;
import com.campus360.finance.repository.RefundRepository;
import com.campus360.finance.repository.FinanceStatusHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class RefundService {

    private final RefundRepository refundRepository;
    private final FinanceStatusHistoryRepository historyRepository;

    @Transactional
    public Refund initiateRefund(Refund refund, String actorId) {
        refund.setStatus("INITIATED");
        Refund saved = refundRepository.save(refund);
        recordHistory(saved, null, "INITIATED", actorId, "Refund initiated");
        return saved;
    }

    @Transactional
    public Refund processRefund(Long tenantId, Long refundId, String actorId) {
        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new IllegalArgumentException("Refund not found"));

        if (!refund.getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException("Tenant mismatch");
        }

        String oldStatus = refund.getStatus();
        refund.setStatus("PROCESSED");
        refund.setProcessedBy(actorId);
        refund.setProcessedAt(Instant.now());

        Refund saved = refundRepository.save(refund);
        recordHistory(saved, oldStatus, "PROCESSED", actorId, "Refund processed successfully");
        return saved;
    }

    private void recordHistory(Refund refund, String oldStatus, String newStatus, String actor, String comments) {
        FinanceStatusHistory history = new FinanceStatusHistory();
        history.setTenantId(refund.getTenantId());
        history.setEntityType("REFUND");
        history.setEntityId(refund.getId());
        history.setPreviousStatus(oldStatus);
        history.setNewStatus(newStatus);
        history.setChangedBy(actor);
        history.setComments(comments);
        historyRepository.save(history);
    }
}
