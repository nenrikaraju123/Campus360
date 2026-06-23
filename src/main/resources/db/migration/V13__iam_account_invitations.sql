-- ============================================================
-- V13: IAM Account Invitations & Welcome Notification Jobs
-- ============================================================

CREATE TABLE account_invitations (
    id          BIGSERIAL PRIMARY KEY,
    tenant_id   BIGINT NOT NULL REFERENCES institutions(id),
    email       VARCHAR(180) NOT NULL,
    role_id     BIGINT NOT NULL REFERENCES roles(id),
    token       VARCHAR(100) NOT NULL UNIQUE,
    status      VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    expires_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    created_by  VARCHAR(120),
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_acc_inv_tenant_email ON account_invitations(tenant_id, email);
CREATE INDEX idx_acc_inv_token ON account_invitations(token);

CREATE TABLE welcome_notification_jobs (
    id            BIGSERIAL PRIMARY KEY,
    tenant_id     BIGINT NOT NULL REFERENCES institutions(id),
    user_id       BIGINT NOT NULL REFERENCES users(id),
    template_name VARCHAR(100) NOT NULL,
    status        VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    error_message TEXT,
    created_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_wnj_tenant_status ON welcome_notification_jobs(tenant_id, status);
