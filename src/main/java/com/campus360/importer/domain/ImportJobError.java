package com.campus360.importer.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.Instant;

@Entity
@Table(name = "import_job_errors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ImportJobError {

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

    @Column(name = "error_code", nullable = false, length = 100)
    private String errorCode;

    @Column(name = "error_message", nullable = false, columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "column_name", length = 100)
    private String columnName;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}
