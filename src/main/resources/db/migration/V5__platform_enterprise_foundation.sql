-- V5: Enterprise Platform Foundation
-- Covers: tenant plans, settings, numbering service, and bulk import framework.

-- ==========================================================
-- 1. Tenant Plans and Subscriptions
-- ==========================================================
CREATE TABLE tenant_plans (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    max_users INT NOT NULL DEFAULT 0,
    max_storage_gb INT NOT NULL DEFAULT 0,
    features_json JSONB,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE tenant_subscriptions (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL UNIQUE,
    plan_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL, -- TRIAL, ACTIVE, SUSPENDED, EXPIRED
    start_date DATE NOT NULL,
    end_date DATE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_subscription_tenant FOREIGN KEY (tenant_id) REFERENCES institutions (id),
    CONSTRAINT fk_subscription_plan FOREIGN KEY (plan_id) REFERENCES tenant_plans (id)
);

CREATE TABLE tenant_settings (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL UNIQUE,
    logo_url VARCHAR(255),
    academic_year VARCHAR(20),
    grading_mode VARCHAR(50),
    attendance_minimum_percentage DOUBLE PRECISION,
    fee_due_reminder_days INT,
    placement_eligibility_defaults JSONB,
    notification_preferences JSONB,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_settings_tenant FOREIGN KEY (tenant_id) REFERENCES institutions (id)
);

-- ==========================================================
-- 2. Numbering Service
-- ==========================================================
CREATE TABLE number_sequences (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    sequence_key VARCHAR(50) NOT NULL,
    prefix VARCHAR(20),
    next_value BIGINT NOT NULL DEFAULT 1,
    padding INT NOT NULL DEFAULT 0,
    financial_year VARCHAR(20),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_tenant_sequence UNIQUE (tenant_id, sequence_key, financial_year),
    CONSTRAINT fk_sequence_tenant FOREIGN KEY (tenant_id) REFERENCES institutions (id)
);

-- ==========================================================
-- 3. Enterprise Bulk Import Framework
-- ==========================================================
CREATE TABLE import_templates (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    template_url VARCHAR(500) NOT NULL,
    columns_json TEXT NOT NULL,
    description TEXT,
    created_by VARCHAR(120),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_import_templates_tenant_type UNIQUE (tenant_id, type)
);

CREATE TABLE import_jobs (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    original_file_name VARCHAR(255) NOT NULL,
    storage_document_id VARCHAR(255),
    status VARCHAR(50) NOT NULL, -- UPLOADED, PARSING, VALIDATING, READY_TO_COMMIT, COMMITTING, COMMITTED, FAILED, CANCELLED
    total_rows INT DEFAULT 0,
    valid_rows INT DEFAULT 0,
    invalid_rows INT DEFAULT 0,
    committed_rows INT DEFAULT 0,
    failed_rows INT DEFAULT 0,
    uploaded_by VARCHAR(100),
    committed_by VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    committed_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_job_tenant FOREIGN KEY (tenant_id) REFERENCES institutions (id)
);

CREATE TABLE import_job_rows (
    id BIGSERIAL PRIMARY KEY,
    job_id BIGINT NOT NULL,
    row_index INT NOT NULL,
    data_json JSONB NOT NULL,
    status VARCHAR(50) NOT NULL, -- PENDING, VALID, INVALID, COMMITTED, FAILED
    CONSTRAINT fk_row_job FOREIGN KEY (job_id) REFERENCES import_jobs (id) ON DELETE CASCADE
);

CREATE TABLE import_job_errors (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    job_id BIGINT NOT NULL,
    row_id BIGINT,
    error_code VARCHAR(100) NOT NULL,
    column_name VARCHAR(100),
    error_message TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_error_job FOREIGN KEY (job_id) REFERENCES import_jobs (id) ON DELETE CASCADE,
    CONSTRAINT fk_error_row FOREIGN KEY (row_id) REFERENCES import_job_rows (id) ON DELETE CASCADE
);

CREATE TABLE import_job_commits (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    job_id BIGINT NOT NULL,
    row_id BIGINT NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    entity_id BIGINT NOT NULL,
    committed_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_commit_job FOREIGN KEY (job_id) REFERENCES import_jobs (id) ON DELETE CASCADE,
    CONSTRAINT fk_commit_row FOREIGN KEY (row_id) REFERENCES import_job_rows (id) ON DELETE CASCADE
);

CREATE TABLE tenant_usage_snapshots (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL REFERENCES institutions (id),
    snapshot_date DATE NOT NULL,
    active_students INT DEFAULT 0,
    active_staff INT DEFAULT 0,
    storage_bytes_used BIGINT DEFAULT 0,
    api_calls BIGINT DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_tenant_usage_snapshot UNIQUE (tenant_id, snapshot_date)
);

CREATE INDEX idx_job_tenant ON import_jobs(tenant_id);
CREATE INDEX idx_row_job ON import_job_rows(job_id);
