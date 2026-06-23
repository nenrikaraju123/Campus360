-- ============================================================
-- V8: Admissions Lifecycle
-- ============================================================

CREATE TABLE admission_leads (
    id                  BIGSERIAL PRIMARY KEY,
    tenant_id           BIGINT        NOT NULL,
    first_name          VARCHAR(80)   NOT NULL,
    last_name           VARCHAR(80)   NOT NULL,
    email               VARCHAR(180),
    phone               VARCHAR(30),
    source              VARCHAR(50),   -- WEBSITE, WALK_IN, REFERRAL, CAMPAIGN
    program_interest    VARCHAR(120),
    status              VARCHAR(30)   NOT NULL DEFAULT 'NEW', -- NEW, CONTACTED, CONVERTED, DISQUALIFIED
    assigned_to         BIGINT,        -- staff user id
    notes               TEXT,
    created_by          BIGINT,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_lead_tenant_status ON admission_leads(tenant_id, status);
CREATE INDEX idx_lead_tenant_email  ON admission_leads(tenant_id, email);

-- -------------------------------------------------------

CREATE TABLE admission_applications (
    id                      BIGSERIAL PRIMARY KEY,
    tenant_id               BIGINT        NOT NULL,
    lead_id                 BIGINT        REFERENCES admission_leads(id),
    first_name              VARCHAR(80)   NOT NULL,
    last_name               VARCHAR(80)   NOT NULL,
    email                   VARCHAR(180)  NOT NULL,
    phone                   VARCHAR(30),
    date_of_birth           DATE,
    gender                  VARCHAR(20),
    category                VARCHAR(50),   -- GENERAL, OBC, SC, ST, EWS
    quota                   VARCHAR(50),
    program_id              BIGINT,
    department_id           BIGINT,
    preferred_section_id    BIGINT,
    academic_year           VARCHAR(20),
    application_number      VARCHAR(50),   -- assigned by NumberingService
    status                  VARCHAR(50)   NOT NULL DEFAULT 'APPLICATION_RECEIVED',
    assigned_reviewer       BIGINT,
    guardian_name           VARCHAR(120),
    guardian_email          VARCHAR(180),
    guardian_phone          VARCHAR(30),
    created_by              BIGINT,
    created_at              TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    version                 INT           NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX idx_app_number_tenant ON admission_applications(tenant_id, application_number)
    WHERE application_number IS NOT NULL;
CREATE INDEX idx_app_tenant_status ON admission_applications(tenant_id, status);
CREATE INDEX idx_app_tenant_email  ON admission_applications(tenant_id, email);

-- -------------------------------------------------------

CREATE TABLE admission_application_documents (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT        NOT NULL,
    application_id  BIGINT        NOT NULL REFERENCES admission_applications(id) ON DELETE CASCADE,
    document_type   VARCHAR(80)   NOT NULL,  -- ID_PROOF, MARKSHEET, PHOTO, etc.
    original_file_name VARCHAR(255),
    file_path       VARCHAR(500)  NOT NULL,
    status          VARCHAR(50)   NOT NULL DEFAULT 'PENDING',
    verified_at     TIMESTAMP WITH TIME ZONE,
    verified_by     VARCHAR(120),
    remarks         VARCHAR(500),
    uploaded_by     VARCHAR(120),
    uploaded_at     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_app_doc_application ON admission_application_documents(application_id);

-- -------------------------------------------------------

CREATE TABLE admission_status_history (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT        NOT NULL,
    application_id  BIGINT        NOT NULL REFERENCES admission_applications(id) ON DELETE CASCADE,
    from_status     VARCHAR(50),
    to_status       VARCHAR(50)   NOT NULL,
    comment         TEXT,
    actor_id        BIGINT,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_app_status_history ON admission_status_history(application_id, created_at DESC);

-- -------------------------------------------------------

CREATE TABLE admission_notes (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT        NOT NULL,
    application_id  BIGINT        NOT NULL REFERENCES admission_applications(id) ON DELETE CASCADE,
    content         TEXT          NOT NULL,
    is_internal     BOOLEAN       NOT NULL DEFAULT TRUE,
    created_by      BIGINT,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_admission_notes_app ON admission_notes(application_id);

-- -------------------------------------------------------

CREATE TABLE admission_offers (
    id                  BIGSERIAL PRIMARY KEY,
    tenant_id           BIGINT        NOT NULL,
    application_id      BIGINT        NOT NULL UNIQUE REFERENCES admission_applications(id),
    offer_date          DATE          NOT NULL,
    expiry_date         DATE,
    program_id          BIGINT,
    department_id       BIGINT,
    section_id          BIGINT,
    academic_year       VARCHAR(20),
    status              VARCHAR(30)   NOT NULL DEFAULT 'ISSUED', -- ISSUED, ACCEPTED, DECLINED, EXPIRED, CANCELLED
    conditions          TEXT,
    issued_by           BIGINT,
    accepted_at         TIMESTAMP WITH TIME ZONE,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_offer_tenant_status ON admission_offers(tenant_id, status);
