-- ============================================================
-- Campus360 V1 : Core schema (IAM, tenancy, institution, people)
-- ============================================================

-- ---- Tenant root ----
CREATE TABLE institutions (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(200) NOT NULL,
    code        VARCHAR(40)  NOT NULL UNIQUE,
    type        VARCHAR(40)  NOT NULL DEFAULT 'UNIVERSITY',
    address     VARCHAR(500),
    status      VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by  VARCHAR(120),
    updated_by  VARCHAR(120)
);

-- ---- Roles ----
CREATE TABLE roles (
    id    BIGSERIAL PRIMARY KEY,
    name  VARCHAR(40) NOT NULL UNIQUE
);

INSERT INTO roles(name) VALUES
    ('SUPER_ADMIN'), ('INSTITUTION_ADMIN'), ('HOD'), ('FACULTY'),
    ('STUDENT'), ('PLACEMENT_OFFICER'), ('RECRUITER'), ('PARENT'), ('ALUMNI');

-- ---- Users ----
CREATE TABLE users (
    id             BIGSERIAL PRIMARY KEY,
    tenant_id      BIGINT REFERENCES institutions(id),   -- NULL for platform SUPER_ADMIN
    email          VARCHAR(180) NOT NULL,
    password_hash  VARCHAR(100) NOT NULL,
    full_name      VARCHAR(160) NOT NULL,
    status         VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    mfa_secret     VARCHAR(120),
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by     VARCHAR(120),
    updated_by     VARCHAR(120),
    CONSTRAINT uq_users_tenant_email UNIQUE (tenant_id, email)
);

CREATE TABLE user_roles (
    user_id  BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id  BIGINT NOT NULL REFERENCES roles(id),
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE refresh_tokens (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash  VARCHAR(120) NOT NULL UNIQUE,
    expires_at  TIMESTAMPTZ NOT NULL,
    revoked     BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_refresh_tokens_user ON refresh_tokens(user_id);

-- ---- Academic structure ----
CREATE TABLE departments (
    id          BIGSERIAL PRIMARY KEY,
    tenant_id   BIGINT NOT NULL REFERENCES institutions(id),
    name        VARCHAR(160) NOT NULL,
    code        VARCHAR(40)  NOT NULL,
    hod_user_id BIGINT REFERENCES users(id),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by  VARCHAR(120),
    updated_by  VARCHAR(120),
    CONSTRAINT uq_departments_tenant_code UNIQUE (tenant_id, code)
);

CREATE TABLE programs (
    id             BIGSERIAL PRIMARY KEY,
    tenant_id      BIGINT NOT NULL REFERENCES institutions(id),
    department_id  BIGINT NOT NULL REFERENCES departments(id),
    name           VARCHAR(160) NOT NULL,
    code           VARCHAR(40)  NOT NULL,
    level          VARCHAR(40)  NOT NULL DEFAULT 'UNDERGRADUATE',
    duration_terms INT          NOT NULL DEFAULT 8,
    total_credits  INT          NOT NULL DEFAULT 160,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by     VARCHAR(120),
    updated_by     VARCHAR(120),
    CONSTRAINT uq_programs_tenant_code UNIQUE (tenant_id, code)
);

CREATE TABLE courses (
    id             BIGSERIAL PRIMARY KEY,
    tenant_id      BIGINT NOT NULL REFERENCES institutions(id),
    department_id  BIGINT NOT NULL REFERENCES departments(id),
    code           VARCHAR(40)  NOT NULL,
    title          VARCHAR(200) NOT NULL,
    credit_hours   INT          NOT NULL DEFAULT 3,
    type           VARCHAR(30)  NOT NULL DEFAULT 'CORE',
    description    VARCHAR(1000),
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by     VARCHAR(120),
    updated_by     VARCHAR(120),
    CONSTRAINT uq_courses_tenant_code UNIQUE (tenant_id, code)
);

CREATE TABLE academic_terms (
    id           BIGSERIAL PRIMARY KEY,
    tenant_id    BIGINT NOT NULL REFERENCES institutions(id),
    name         VARCHAR(80) NOT NULL,
    start_date   DATE,
    end_date     DATE,
    add_drop_end DATE,
    status       VARCHAR(20) NOT NULL DEFAULT 'PLANNED',
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by   VARCHAR(120),
    updated_by   VARCHAR(120)
);

CREATE TABLE sections (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT NOT NULL REFERENCES institutions(id),
    course_id       BIGINT NOT NULL REFERENCES courses(id),
    term_id         BIGINT NOT NULL REFERENCES academic_terms(id),
    faculty_user_id BIGINT REFERENCES users(id),
    capacity        INT NOT NULL DEFAULT 60,
    schedule        VARCHAR(200),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by      VARCHAR(120),
    updated_by      VARCHAR(120)
);
CREATE INDEX idx_sections_term ON sections(tenant_id, term_id);

-- ---- People profiles ----
CREATE TABLE student_profiles (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT NOT NULL REFERENCES institutions(id),
    user_id         BIGINT NOT NULL UNIQUE REFERENCES users(id),
    program_id      BIGINT REFERENCES programs(id),
    roll_number     VARCHAR(40) NOT NULL,
    branch          VARCHAR(80),
    batch_year      INT,
    admission_date  DATE,
    current_term    INT NOT NULL DEFAULT 1,
    cgpa            NUMERIC(4,2) NOT NULL DEFAULT 0.00,
    active_backlogs INT NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by      VARCHAR(120),
    updated_by      VARCHAR(120),
    CONSTRAINT uq_student_tenant_roll UNIQUE (tenant_id, roll_number)
);

CREATE TABLE faculty_profiles (
    id             BIGSERIAL PRIMARY KEY,
    tenant_id      BIGINT NOT NULL REFERENCES institutions(id),
    user_id        BIGINT NOT NULL UNIQUE REFERENCES users(id),
    department_id  BIGINT REFERENCES departments(id),
    designation    VARCHAR(80),
    specialization VARCHAR(160),
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by     VARCHAR(120),
    updated_by     VARCHAR(120)
);
