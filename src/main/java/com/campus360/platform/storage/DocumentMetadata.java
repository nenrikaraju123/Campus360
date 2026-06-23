package com.campus360.platform.storage;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "document_metadata")
public class DocumentMetadata {

    @Id
    @Column(length = 255)
    private String id; // UUID or path

    private Long tenantId;

    @Column(nullable = false)
    private String originalFileName;

    @Column(nullable = false)
    private String contentType;

    @Column(nullable = false)
    private Long sizeBytes;

    @Column(nullable = false)
    private String storageProvider; // LOCAL, S3

    @Column(nullable = false)
    private String physicalPath; // Relative path or S3 key

    private Long uploadedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }

    public String getOriginalFileName() { return originalFileName; }
    public void setOriginalFileName(String originalFileName) { this.originalFileName = originalFileName; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public Long getSizeBytes() { return sizeBytes; }
    public void setSizeBytes(Long sizeBytes) { this.sizeBytes = sizeBytes; }

    public String getStorageProvider() { return storageProvider; }
    public void setStorageProvider(String storageProvider) { this.storageProvider = storageProvider; }

    public String getPhysicalPath() { return physicalPath; }
    public void setPhysicalPath(String physicalPath) { this.physicalPath = physicalPath; }

    public Long getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(Long uploadedBy) { this.uploadedBy = uploadedBy; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
