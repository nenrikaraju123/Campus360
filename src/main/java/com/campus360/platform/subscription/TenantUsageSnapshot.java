package com.campus360.platform.subscription;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "tenant_usage_snapshots")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TenantUsageSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "snapshot_date", nullable = false)
    private LocalDate snapshotDate;

    @Column(name = "active_students")
    private Integer activeStudents = 0;

    @Column(name = "active_staff")
    private Integer activeStaff = 0;

    @Column(name = "storage_bytes_used")
    private Long storageBytesUsed = 0L;

    @Column(name = "api_calls")
    private Long apiCalls = 0L;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}
