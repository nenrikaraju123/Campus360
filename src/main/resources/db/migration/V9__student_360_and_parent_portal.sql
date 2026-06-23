-- ============================================================
-- V9: Student 360 and Parent Portal
-- ============================================================

-- Extend student_profiles with lifecycle and personal fields
ALTER TABLE student_profiles
    ADD COLUMN IF NOT EXISTS admission_number       VARCHAR(50),
    ADD COLUMN IF NOT EXISTS enrollment_date        DATE,
    ADD COLUMN IF NOT EXISTS lifecycle_status       VARCHAR(30)  NOT NULL DEFAULT 'ACTIVE',
    ADD COLUMN IF NOT EXISTS category               VARCHAR(50),
    ADD COLUMN IF NOT EXISTS quota                  VARCHAR(50),
    ADD COLUMN IF NOT EXISTS blood_group            VARCHAR(10),
    ADD COLUMN IF NOT EXISTS date_of_birth          DATE,
    ADD COLUMN IF NOT EXISTS gender                 VARCHAR(20),
    ADD COLUMN IF NOT EXISTS nationality            VARCHAR(80),
    ADD COLUMN IF NOT EXISTS emergency_contact_name VARCHAR(120),
    ADD COLUMN IF NOT EXISTS emergency_contact_phone VARCHAR(30),
    ADD COLUMN IF NOT EXISTS current_academic_standing VARCHAR(30) DEFAULT 'GOOD_STANDING';

CREATE UNIQUE INDEX IF NOT EXISTS idx_student_admission_number
    ON student_profiles(tenant_id, admission_number)
    WHERE admission_number IS NOT NULL;

-- -------------------------------------------------------

CREATE TABLE student_guardians (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT        NOT NULL,
    student_id      BIGINT        NOT NULL REFERENCES student_profiles(id) ON DELETE CASCADE,
    full_name       VARCHAR(120)  NOT NULL,
    relationship    VARCHAR(50)   NOT NULL,  -- FATHER, MOTHER, GUARDIAN
    email           VARCHAR(180),
    phone           VARCHAR(30),
    occupation      VARCHAR(80),
    annual_income   NUMERIC(14,2),
    is_primary      BOOLEAN       NOT NULL DEFAULT FALSE,
    create_portal_account BOOLEAN NOT NULL DEFAULT FALSE,
    user_id         BIGINT,       -- linked IAM user if portal access created
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_guardian_student    ON student_guardians(student_id);
CREATE INDEX idx_guardian_tenant     ON student_guardians(tenant_id);

-- -------------------------------------------------------

CREATE TABLE student_addresses (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT        NOT NULL,
    student_id      BIGINT        NOT NULL REFERENCES student_profiles(id) ON DELETE CASCADE,
    address_type    VARCHAR(30)   NOT NULL,  -- CURRENT, PERMANENT
    address_line_1  VARCHAR(255),
    address_line_2  VARCHAR(255),
    city            VARCHAR(80),
    state           VARCHAR(80),
    pincode         VARCHAR(20),
    country         VARCHAR(80)   DEFAULT 'India',
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_student_address ON student_addresses(student_id);

-- -------------------------------------------------------

CREATE TABLE student_documents (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT        NOT NULL,
    student_id      BIGINT        NOT NULL REFERENCES student_profiles(id) ON DELETE CASCADE,
    document_type   VARCHAR(80)   NOT NULL,
    original_file_name VARCHAR(255),
    file_path       VARCHAR(500)  NOT NULL,
    status          VARCHAR(30)   NOT NULL DEFAULT 'UPLOADED',
    is_verified     BOOLEAN       NOT NULL DEFAULT FALSE,
    verified_by     BIGINT,
    remarks         VARCHAR(500),
    uploaded_by     VARCHAR(120),
    uploaded_at     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_student_doc_student ON student_documents(student_id);

-- -------------------------------------------------------

CREATE TABLE student_lifecycle_history (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT        NOT NULL,
    student_id      BIGINT        NOT NULL REFERENCES student_profiles(id),
    from_status     VARCHAR(30),
    to_status       VARCHAR(30)   NOT NULL,
    action          VARCHAR(50),   -- PROMOTED, SUSPENDED, GRADUATED, ARCHIVED, TRANSFERRED
    comment         TEXT,
    actor_id        BIGINT,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_student_lifecycle ON student_lifecycle_history(student_id, created_at DESC);

-- -------------------------------------------------------

CREATE TABLE student_notes (
    id          BIGSERIAL PRIMARY KEY,
    tenant_id   BIGINT        NOT NULL,
    student_id  BIGINT        NOT NULL REFERENCES student_profiles(id),
    content     TEXT          NOT NULL,
    is_internal BOOLEAN       NOT NULL DEFAULT TRUE,
    created_by  VARCHAR(120),
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_student_notes ON student_notes(student_id);

-- -------------------------------------------------------

CREATE TABLE student_tags (
    id          BIGSERIAL PRIMARY KEY,
    tenant_id   BIGINT        NOT NULL,
    name        VARCHAR(100)  NOT NULL,
    color_hex   VARCHAR(10),
    created_by  VARCHAR(120),
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    UNIQUE(tenant_id, name)
);

CREATE TABLE student_tag_links (
    id          BIGSERIAL PRIMARY KEY,
    tenant_id   BIGINT        NOT NULL,
    student_id  BIGINT NOT NULL REFERENCES student_profiles(id) ON DELETE CASCADE,
    tag_id      BIGINT NOT NULL REFERENCES student_tags(id) ON DELETE CASCADE,
    linked_by   VARCHAR(120),
    linked_at   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    UNIQUE(tenant_id, student_id, tag_id)
);

-- -------------------------------------------------------

CREATE TABLE parent_student_links (
    id          BIGSERIAL PRIMARY KEY,
    tenant_id   BIGINT    NOT NULL,
    parent_id   BIGINT    NOT NULL,   -- IAM user with PARENT role
    student_id  BIGINT    NOT NULL REFERENCES student_profiles(id),
    relationship VARCHAR(50) NOT NULL DEFAULT 'GUARDIAN',
    is_active   BOOLEAN   NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    UNIQUE(tenant_id, parent_id, student_id)
);

CREATE INDEX idx_parent_link_parent  ON parent_student_links(parent_id);
CREATE INDEX idx_parent_link_student ON parent_student_links(student_id);
