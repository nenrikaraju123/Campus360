package com.campus360.finance.service;

import com.campus360.finance.domain.ReconciliationBatch;
import com.campus360.finance.domain.ReconciliationItem;
import com.campus360.finance.repository.ReconciliationBatchRepository;
import com.campus360.finance.repository.ReconciliationItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReconciliationService {

    private final ReconciliationBatchRepository batchRepository;
    private final ReconciliationItemRepository itemRepository;

    @Transactional
    public ReconciliationBatch uploadReconciliationData(ReconciliationBatch batch, List<ReconciliationItem> items, String actorId) {
        batch.setStatus("PENDING");
        batch.setTotalRecords(items.size());
        batch.setUploadedBy(actorId);
        
        ReconciliationBatch savedBatch = batchRepository.save(batch);

        for (ReconciliationItem item : items) {
            item.setBatch(savedBatch);
            itemRepository.save(item);
        }

        return savedBatch;
    }

    @Transactional
    public ReconciliationBatch completeReconciliation(Long tenantId, Long batchId, String actorId) {
        ReconciliationBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new IllegalArgumentException("Batch not found"));

        if (!batch.getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException("Tenant mismatch");
        }

        // Simplistic logic for completion
        batch.setStatus("COMPLETED");
        batch.setCompletedAt(Instant.now());
        return batchRepository.save(batch);
    }
}
