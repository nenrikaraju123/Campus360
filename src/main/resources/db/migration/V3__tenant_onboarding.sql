-- ============================================================
-- Campus360 V3 : Tenant self-registration & platform-admin approval
-- ============================================================

-- First-login password rotation for provisioned admins.
ALTER TABLE users ADD COLUMN must_change_password BOOLEAN NOT NULL DEFAULT FALSE;

-- Public registration requests reviewed by the platform SUPER_ADMIN.
CREATE TABLE tenant_registrations (
    id                BIGSERIAL PRIMARY KEY,
    institution_name  VARCHAR(200) NOT NULL,
    institution_code  VARCHAR(40)  NOT NULL,
    type              VARCHAR(40)  NOT NULL DEFAULT 'UNIVERSITY',
    admin_full_name   VARCHAR(160) NOT NULL,
    admin_email       VARCHAR(180) NOT NULL,
    contact_phone     VARCHAR(40),
    message           VARCHAR(1000),
    status            VARCHAR(20)  NOT NULL DEFAULT 'PENDING',  -- PENDING / APPROVED / REJECTED
    review_notes      VARCHAR(1000),
    reviewed_by       VARCHAR(120),
    reviewed_at       TIMESTAMPTZ,
    institution_id    BIGINT REFERENCES institutions(id),       -- set on approval
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_tenant_registrations_status ON tenant_registrations(status);
CREATE INDEX idx_tenant_registrations_code ON tenant_registrations(lower(institution_code));
