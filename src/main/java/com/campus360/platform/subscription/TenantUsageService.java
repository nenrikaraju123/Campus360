package com.campus360.platform.subscription;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class TenantUsageService {

    private final TenantUsageSnapshotRepository repository;

    @Transactional
    public void recordApiCall(Long tenantId) {
        LocalDate today = LocalDate.now();
        TenantUsageSnapshot snapshot = repository.findByTenantIdAndSnapshotDate(tenantId, today)
                .orElseGet(() -> {
                    TenantUsageSnapshot newSnapshot = new TenantUsageSnapshot();
                    newSnapshot.setTenantId(tenantId);
                    newSnapshot.setSnapshotDate(today);
                    return newSnapshot;
                });
        
        snapshot.setApiCalls(snapshot.getApiCalls() + 1);
        repository.save(snapshot);
    }

    @Transactional
    public void updateStorageUsage(Long tenantId, Long bytesUsed) {
        LocalDate today = LocalDate.now();
        TenantUsageSnapshot snapshot = repository.findByTenantIdAndSnapshotDate(tenantId, today)
                .orElseGet(() -> {
                    TenantUsageSnapshot newSnapshot = new TenantUsageSnapshot();
                    newSnapshot.setTenantId(tenantId);
                    newSnapshot.setSnapshotDate(today);
                    return newSnapshot;
                });

        snapshot.setStorageBytesUsed(bytesUsed);
        repository.save(snapshot);
    }
}
