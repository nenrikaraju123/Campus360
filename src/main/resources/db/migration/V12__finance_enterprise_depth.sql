-- ============================================================
-- V12: Finance Enterprise Depth
-- ============================================================

-- ---- Fee Plans & Components ----

CREATE TABLE fee_categories (
    id            BIGSERIAL PRIMARY KEY,
    tenant_id     BIGINT       NOT NULL,
    code          VARCHAR(50)  NOT NULL,
    name          VARCHAR(200) NOT NULL,
    description   VARCHAR(500),
    is_active     BOOLEAN      DEFAULT TRUE,
    created_at    TIMESTAMPTZ  DEFAULT now(),
    updated_at    TIMESTAMPTZ  DEFAULT now(),
    created_by    VARCHAR(120),
    updated_by    VARCHAR(120),
    CONSTRAINT uq_fee_cat_code UNIQUE (tenant_id, code)
);

CREATE TABLE fee_components (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT       NOT NULL,
    category_id     BIGINT       NOT NULL REFERENCES fee_categories(id),
    code            VARCHAR(50)  NOT NULL,
    name            VARCHAR(200) NOT NULL,
    amount          DECIMAL(10,2) NOT NULL,
    is_mandatory    BOOLEAN      DEFAULT TRUE,
    is_refundable   BOOLEAN      DEFAULT FALSE,
    tax_percentage  DECIMAL(5,2) DEFAULT 0.00,
    created_at      TIMESTAMPTZ  DEFAULT now(),
    updated_at      TIMESTAMPTZ  DEFAULT now(),
    created_by      VARCHAR(120),
    updated_by      VARCHAR(120),
    CONSTRAINT uq_fee_comp_code UNIQUE (tenant_id, code)
);

CREATE TABLE student_fee_assignments (
    id            BIGSERIAL PRIMARY KEY,
    tenant_id     BIGINT NOT NULL,
    student_id    BIGINT NOT NULL, -- references student_profiles
    category_id   BIGINT NOT NULL REFERENCES fee_categories(id),
    academic_year VARCHAR(20),
    term_id       BIGINT, -- references academic_terms
    assigned_at   TIMESTAMPTZ DEFAULT now(),
    assigned_by   VARCHAR(120),
    status        VARCHAR(30) DEFAULT 'ACTIVE', -- ACTIVE, CANCELLED
    CONSTRAINT uq_student_fee_assign UNIQUE (tenant_id, student_id, category_id, term_id)
);

-- ---- Invoices & Line Items ----
-- Modifying existing invoices table conceptually, but if it exists we might need to alter it.
-- Let's check if invoices already exists from previous migrations. If so, we alter. If not, we create.
-- Wait, the backend plan says "Current finance exists. Deepen it instead of rewriting it. New or extended tables... invoice_line_items".
-- Assuming invoices exists. Let's add invoice_line_items.

CREATE TABLE invoice_line_items (
    id               BIGSERIAL PRIMARY KEY,
    tenant_id        BIGINT NOT NULL,
    invoice_id       BIGINT NOT NULL, -- references invoices
    fee_component_id BIGINT REFERENCES fee_components(id),
    description      VARCHAR(255) NOT NULL,
    amount           DECIMAL(10,2) NOT NULL,
    tax_amount       DECIMAL(10,2) DEFAULT 0.00,
    total_amount     DECIMAL(10,2) NOT NULL,
    allocated_amount DECIMAL(10,2) DEFAULT 0.00,
    waived_amount    DECIMAL(10,2) DEFAULT 0.00,
    balance_amount   DECIMAL(10,2) NOT NULL,
    created_at       TIMESTAMPTZ DEFAULT now()
);
CREATE INDEX idx_ili_invoice ON invoice_line_items (tenant_id, invoice_id);

-- ---- Receipts & Payments ----

CREATE TABLE receipts (
    id               BIGSERIAL PRIMARY KEY,
    tenant_id        BIGINT NOT NULL,
    receipt_number   VARCHAR(50) NOT NULL,
    payment_id       BIGINT NOT NULL, -- references payments
    student_id       BIGINT NOT NULL,
    amount           DECIMAL(10,2) NOT NULL,
    issued_at        TIMESTAMPTZ DEFAULT now(),
    issued_by        VARCHAR(120),
    status           VARCHAR(30) DEFAULT 'ISSUED', -- ISSUED, CANCELLED
    CONSTRAINT uq_receipt_number UNIQUE (tenant_id, receipt_number)
);

CREATE TABLE payment_allocations (
    id               BIGSERIAL PRIMARY KEY,
    tenant_id        BIGINT NOT NULL,
    payment_id       BIGINT NOT NULL,
    invoice_line_id  BIGINT NOT NULL REFERENCES invoice_line_items(id),
    allocated_amount DECIMAL(10,2) NOT NULL,
    allocated_at     TIMESTAMPTZ DEFAULT now(),
    allocated_by     VARCHAR(120)
);
CREATE INDEX idx_pa_payment ON payment_allocations (tenant_id, payment_id);

-- ---- Waivers & Refunds ----

CREATE TABLE fee_concessions (
    id               BIGSERIAL PRIMARY KEY,
    tenant_id        BIGINT NOT NULL,
    student_id       BIGINT NOT NULL,
    category_id      BIGINT REFERENCES fee_categories(id),
    component_id     BIGINT REFERENCES fee_components(id),
    concession_type  VARCHAR(50) NOT NULL, -- PERCENTAGE, FIXED_AMOUNT
    value            DECIMAL(10,2) NOT NULL,
    reason           VARCHAR(500),
    status           VARCHAR(30) DEFAULT 'PENDING', -- PENDING, APPROVED, REJECTED, APPLIED
    approved_by      VARCHAR(120),
    approved_at      TIMESTAMPTZ,
    created_at       TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE fee_waivers (
    id               BIGSERIAL PRIMARY KEY,
    tenant_id        BIGINT NOT NULL,
    invoice_line_id  BIGINT NOT NULL REFERENCES invoice_line_items(id),
    waived_amount    DECIMAL(10,2) NOT NULL,
    reason           VARCHAR(500) NOT NULL,
    approved_by      VARCHAR(120) NOT NULL,
    approved_at      TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE refunds (
    id               BIGSERIAL PRIMARY KEY,
    tenant_id        BIGINT NOT NULL,
    payment_id       BIGINT NOT NULL,
    student_id       BIGINT NOT NULL,
    refund_amount    DECIMAL(10,2) NOT NULL,
    reason           VARCHAR(500),
    status           VARCHAR(30) DEFAULT 'INITIATED', -- INITIATED, PROCESSED, FAILED
    processed_at     TIMESTAMPTZ,
    processed_by     VARCHAR(120),
    created_at       TIMESTAMPTZ DEFAULT now(),
    updated_at       TIMESTAMPTZ DEFAULT now()
);

-- ---- Workflow History ----

CREATE TABLE finance_status_history (
    id               BIGSERIAL PRIMARY KEY,
    tenant_id        BIGINT NOT NULL,
    entity_type      VARCHAR(50) NOT NULL, -- INVOICE, PAYMENT, REFUND, CONCESSION
    entity_id        BIGINT NOT NULL,
    previous_status  VARCHAR(50),
    new_status       VARCHAR(50) NOT NULL,
    changed_by       VARCHAR(120) NOT NULL,
    comments         VARCHAR(500),
    changed_at       TIMESTAMPTZ DEFAULT now()
);
CREATE INDEX idx_fsh_entity ON finance_status_history (tenant_id, entity_type, entity_id);

-- ---- Reconciliation ----

CREATE TABLE reconciliation_batches (
    id               BIGSERIAL PRIMARY KEY,
    tenant_id        BIGINT NOT NULL,
    bank_account_id  BIGINT,
    batch_date       DATE NOT NULL,
    total_records    INT DEFAULT 0,
    matched_records  INT DEFAULT 0,
    status           VARCHAR(30) DEFAULT 'PENDING', -- PENDING, COMPLETED, DISCREPANCY
    uploaded_by      VARCHAR(120),
    uploaded_at      TIMESTAMPTZ DEFAULT now(),
    completed_at     TIMESTAMPTZ
);

CREATE TABLE reconciliation_items (
    id               BIGSERIAL PRIMARY KEY,
    tenant_id        BIGINT NOT NULL,
    batch_id         BIGINT NOT NULL REFERENCES reconciliation_batches(id),
    payment_id       BIGINT,
    bank_reference   VARCHAR(100),
    transaction_date DATE NOT NULL,
    amount           DECIMAL(10,2) NOT NULL,
    status           VARCHAR(30) DEFAULT 'UNMATCHED', -- MATCHED, UNMATCHED, DISPUTED
    remarks          VARCHAR(500),
    created_at       TIMESTAMPTZ DEFAULT now()
);
