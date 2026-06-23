package com.campus360.importer.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.Instant;

@Entity
@Table(name = "import_job_commits")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ImportJobCommit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private ImportJob job;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "row_id", nullable = false)
    private ImportJobRow row;

    @Column(name = "entity_type", nullable = false, length = 100)
    private String entityType;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Column(name = "committed_at", nullable = false, updatable = false)
    private Instant committedAt = Instant.now();
}
