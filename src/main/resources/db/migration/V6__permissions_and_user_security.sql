-- V6: Fine-Grained Permissions, Security, and Outbox Pattern

-- ==========================================================
-- 1. Fine-Grained Permissions
-- ==========================================================
CREATE TABLE permissions (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    module VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE role_permissions (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_role_perm_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE,
    CONSTRAINT fk_role_perm_perm FOREIGN KEY (permission_id) REFERENCES permissions (id) ON DELETE CASCADE
);

-- ==========================================================
-- 2. Login Audit and Account Security
-- ==========================================================
CREATE TABLE user_login_events (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT, -- Can be null if login fails for non-existent user
    email VARCHAR(255) NOT NULL,
    tenant_id BIGINT,
    status VARCHAR(50) NOT NULL, -- SUCCESS, FAILED, LOCKED
    ip_address VARCHAR(100),
    user_agent TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE account_lock_events (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    reason VARCHAR(255) NOT NULL,
    locked_at TIMESTAMP WITH TIME ZONE NOT NULL,
    unlocked_at TIMESTAMP WITH TIME ZONE,
    unlocked_by VARCHAR(100),
    CONSTRAINT fk_lock_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_login_email ON user_login_events(email);
CREATE INDEX idx_login_user ON user_login_events(user_id);

-- ==========================================================
-- 3. Outbox Pattern for Notifications
-- ==========================================================
CREATE TABLE outbox_messages (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT,
    type VARCHAR(50) NOT NULL, -- EMAIL, SMS, NOTIFICATION
    recipient VARCHAR(255) NOT NULL,
    subject VARCHAR(255),
    payload TEXT NOT NULL,
    status VARCHAR(50) NOT NULL, -- PENDING, SENT, FAILED
    retry_count INT NOT NULL DEFAULT 0,
    next_retry_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    processed_at TIMESTAMP WITH TIME ZONE,
    error_message TEXT
);

CREATE INDEX idx_outbox_status ON outbox_messages(status, next_retry_at);
