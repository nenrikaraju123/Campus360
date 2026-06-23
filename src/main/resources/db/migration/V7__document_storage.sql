-- V7: Document Storage

CREATE TABLE document_metadata (
    id VARCHAR(255) PRIMARY KEY,
    tenant_id BIGINT,
    original_file_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    size_bytes BIGINT NOT NULL,
    storage_provider VARCHAR(50) NOT NULL,
    physical_path VARCHAR(500) NOT NULL,
    uploaded_by BIGINT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_document_tenant ON document_metadata(tenant_id);
