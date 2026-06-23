package com.campus360.importer.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "import_jobs")
public class ImportJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long tenantId;

    @Column(nullable = false, length = 50)
    private String type;

    @Column(nullable = false)
    private String originalFileName;

    private String storageDocumentId;

    @Column(nullable = false, length = 50)
    private String status;

    private Integer totalRows = 0;
    private Integer validRows = 0;
    private Integer invalidRows = 0;
    private Integer committedRows = 0;
    private Integer failedRows = 0;

    private String uploadedBy;
    private String committedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    private Instant committedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getOriginalFileName() { return originalFileName; }
    public void setOriginalFileName(String originalFileName) { this.originalFileName = originalFileName; }

    public String getStorageDocumentId() { return storageDocumentId; }
    public void setStorageDocumentId(String storageDocumentId) { this.storageDocumentId = storageDocumentId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getTotalRows() { return totalRows; }
    public void setTotalRows(Integer totalRows) { this.totalRows = totalRows; }

    public Integer getValidRows() { return validRows; }
    public void setValidRows(Integer validRows) { this.validRows = validRows; }

    public Integer getInvalidRows() { return invalidRows; }
    public void setInvalidRows(Integer invalidRows) { this.invalidRows = invalidRows; }

    public Integer getCommittedRows() { return committedRows; }
    public void setCommittedRows(Integer committedRows) { this.committedRows = committedRows; }

    public Integer getFailedRows() { return failedRows; }
    public void setFailedRows(Integer failedRows) { this.failedRows = failedRows; }

    public String getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(String uploadedBy) { this.uploadedBy = uploadedBy; }

    public String getCommittedBy() { return committedBy; }
    public void setCommittedBy(String committedBy) { this.committedBy = committedBy; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public Instant getCommittedAt() { return committedAt; }
    public void setCommittedAt(Instant committedAt) { this.committedAt = committedAt; }
}
