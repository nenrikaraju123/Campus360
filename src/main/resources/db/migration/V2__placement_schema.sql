-- ============================================================
-- Campus360 V2 : Placement & Career module
-- ============================================================

CREATE TABLE companies (
    id          BIGSERIAL PRIMARY KEY,
    tenant_id   BIGINT NOT NULL REFERENCES institutions(id),
    name        VARCHAR(200) NOT NULL,
    sector      VARCHAR(80),
    tier        VARCHAR(20),                 -- DREAM / TIER1 / TIER2 / MASS
    website     VARCHAR(200),
    description VARCHAR(2000),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by  VARCHAR(120),
    updated_by  VARCHAR(120)
);
CREATE INDEX idx_companies_tenant ON companies(tenant_id);

CREATE TABLE job_postings (
    id           BIGSERIAL PRIMARY KEY,
    tenant_id    BIGINT NOT NULL REFERENCES institutions(id),
    company_id   BIGINT NOT NULL REFERENCES companies(id),
    title        VARCHAR(200) NOT NULL,
    type         VARCHAR(20) NOT NULL DEFAULT 'FULL_TIME',  -- FULL_TIME / INTERNSHIP / PPO
    ctc          NUMERIC(12,2),
    location     VARCHAR(160),
    description  VARCHAR(4000),
    -- Eligibility stored as JSON text: {minCgpa, branches[], maxBacklogs, batchYear}
    eligibility  TEXT,
    status       VARCHAR(20) NOT NULL DEFAULT 'OPEN',        -- DRAFT / OPEN / CLOSED
    posted_by    BIGINT REFERENCES users(id),
    closes_at    TIMESTAMPTZ,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by   VARCHAR(120),
    updated_by   VARCHAR(120)
);
CREATE INDEX idx_postings_tenant_status ON job_postings(tenant_id, status);

CREATE TABLE applications (
    id          BIGSERIAL PRIMARY KEY,
    tenant_id   BIGINT NOT NULL REFERENCES institutions(id),
    posting_id  BIGINT NOT NULL REFERENCES job_postings(id),
    student_id  BIGINT NOT NULL REFERENCES student_profiles(id),
    status      VARCHAR(20) NOT NULL DEFAULT 'APPLIED',     -- APPLIED/SHORTLISTED/REJECTED/OFFERED/WITHDRAWN
    applied_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_application UNIQUE (posting_id, student_id)
);
CREATE INDEX idx_applications_student ON applications(student_id);

CREATE TABLE drives (
    id          BIGSERIAL PRIMARY KEY,
    tenant_id   BIGINT NOT NULL REFERENCES institutions(id),
    posting_id  BIGINT NOT NULL REFERENCES job_postings(id),
    name        VARCHAR(160),
    status      VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE drive_rounds (
    id            BIGSERIAL PRIMARY KEY,
    tenant_id     BIGINT NOT NULL REFERENCES institutions(id),
    drive_id      BIGINT NOT NULL REFERENCES drives(id) ON DELETE CASCADE,
    name          VARCHAR(120) NOT NULL,
    type          VARCHAR(40) NOT NULL,        -- ONLINE_TEST / GD / TECHNICAL / HR
    sequence      INT NOT NULL,
    scheduled_at  TIMESTAMPTZ
);

CREATE TABLE round_results (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT NOT NULL REFERENCES institutions(id),
    round_id        BIGINT NOT NULL REFERENCES drive_rounds(id) ON DELETE CASCADE,
    application_id  BIGINT NOT NULL REFERENCES applications(id),
    result          VARCHAR(20) NOT NULL DEFAULT 'PENDING',   -- PASS / FAIL / PENDING
    feedback        VARCHAR(1000)
);

CREATE TABLE offers (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT NOT NULL REFERENCES institutions(id),
    application_id  BIGINT NOT NULL REFERENCES applications(id),
    ctc             NUMERIC(12,2),
    joining_date    DATE,
    status          VARCHAR(20) NOT NULL DEFAULT 'EXTENDED',  -- EXTENDED/ACCEPTED/DECLINED
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_offers_tenant ON offers(tenant_id);

CREATE TABLE career_profiles (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT NOT NULL REFERENCES institutions(id),
    student_id      BIGINT NOT NULL UNIQUE REFERENCES student_profiles(id),
    resume_ref      VARCHAR(300),
    skills          TEXT,                       -- comma-separated or JSON
    certifications  TEXT,
    readiness_score INT NOT NULL DEFAULT 0,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
