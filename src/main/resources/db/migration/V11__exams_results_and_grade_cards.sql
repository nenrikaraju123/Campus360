-- ============================================================
-- V11: Exams, Results, and Grade Cards
-- ============================================================

-- ---- Exam Cycles & Schedules ----

CREATE TABLE exam_cycles (
    id            BIGSERIAL PRIMARY KEY,
    tenant_id     BIGINT       NOT NULL,
    name          VARCHAR(200) NOT NULL,
    term_id       BIGINT,
    academic_year VARCHAR(20),
    start_date    DATE         NOT NULL,
    end_date      DATE         NOT NULL,
    status        VARCHAR(30)  DEFAULT 'DRAFT',
    created_at    TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_ec_tenant ON exam_cycles (tenant_id);
CREATE INDEX idx_ec_status ON exam_cycles (tenant_id, status);

CREATE TABLE exam_components (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT      NOT NULL,
    exam_cycle_id   BIGINT      NOT NULL,
    course_id       BIGINT      NOT NULL,
    component_name  VARCHAR(50) NOT NULL, -- e.g., MIDTERM, FINAL, PRACTICAL
    max_marks       DECIMAL(6,2) NOT NULL,
    passing_marks   DECIMAL(6,2),
    weightage_pct   DECIMAL(5,2) DEFAULT 100.00,
    created_at      TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_ecomp_cycle ON exam_components (tenant_id, exam_cycle_id);
CREATE INDEX idx_ecomp_course ON exam_components (tenant_id, course_id);

CREATE TABLE exam_schedules (
    id               BIGSERIAL PRIMARY KEY,
    tenant_id        BIGINT NOT NULL,
    exam_cycle_id    BIGINT NOT NULL,
    course_id        BIGINT NOT NULL,
    exam_date        DATE   NOT NULL,
    start_time       TIME   NOT NULL,
    end_time         TIME   NOT NULL,
    room_id          BIGINT,
    invigilator_id   BIGINT,
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_esched_cycle ON exam_schedules (tenant_id, exam_cycle_id);

-- ---- Exam Mark Sheets (Workflow) ----

CREATE TABLE exam_mark_sheets (
    id               BIGSERIAL PRIMARY KEY,
    tenant_id        BIGINT NOT NULL,
    exam_cycle_id    BIGINT NOT NULL,
    course_id        BIGINT NOT NULL,
    section_id       BIGINT NOT NULL,
    faculty_id       BIGINT NOT NULL,
    status           VARCHAR(30) DEFAULT 'DRAFT', -- DRAFT, SUBMITTED, REVIEWED, PUBLISHED
    submitted_at     TIMESTAMP,
    reviewed_at      TIMESTAMP,
    reviewed_by      BIGINT,
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version          INT       DEFAULT 0,
    CONSTRAINT uq_mark_sheet UNIQUE (tenant_id, exam_cycle_id, course_id, section_id)
);
CREATE INDEX idx_ems_tenant ON exam_mark_sheets (tenant_id);
CREATE INDEX idx_ems_faculty ON exam_mark_sheets (tenant_id, faculty_id);
CREATE INDEX idx_ems_status ON exam_mark_sheets (tenant_id, status);

CREATE TABLE exam_marks (
    id                BIGSERIAL PRIMARY KEY,
    tenant_id         BIGINT NOT NULL,
    mark_sheet_id     BIGINT NOT NULL,
    student_id        BIGINT NOT NULL,
    exam_component_id BIGINT NOT NULL,
    marks_obtained    DECIMAL(6,2),
    is_absent         BOOLEAN DEFAULT FALSE,
    remarks           VARCHAR(200),
    created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_exam_mark UNIQUE (tenant_id, mark_sheet_id, student_id, exam_component_id)
);
CREATE INDEX idx_em_sheet ON exam_marks (tenant_id, mark_sheet_id);
CREATE INDEX idx_em_student ON exam_marks (tenant_id, student_id);

-- ---- Results & Grade Cards ----

CREATE TABLE result_publications (
    id               BIGSERIAL PRIMARY KEY,
    tenant_id        BIGINT NOT NULL,
    exam_cycle_id    BIGINT NOT NULL,
    program_id       BIGINT NOT NULL,
    term_id          BIGINT,
    published_at     TIMESTAMP NOT NULL,
    published_by     BIGINT NOT NULL,
    remarks          VARCHAR(500)
);
CREATE INDEX idx_rp_cycle ON result_publications (tenant_id, exam_cycle_id);

CREATE TABLE grade_cards (
    id                  BIGSERIAL PRIMARY KEY,
    tenant_id           BIGINT NOT NULL,
    student_id          BIGINT NOT NULL,
    exam_cycle_id       BIGINT NOT NULL,
    term_id             BIGINT,
    total_credits       DECIMAL(6,2),
    earned_credits      DECIMAL(6,2),
    sgpa                DECIMAL(4,2),
    cgpa                DECIMAL(4,2),
    result_status       VARCHAR(30), -- PASS, FAIL, PROMOTED, WITHHELD
    is_published        BOOLEAN DEFAULT FALSE,
    published_at        TIMESTAMP,
    generated_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_grade_card UNIQUE (tenant_id, student_id, exam_cycle_id)
);
CREATE INDEX idx_gc_student ON grade_cards (tenant_id, student_id);
CREATE INDEX idx_gc_cycle ON grade_cards (tenant_id, exam_cycle_id);

-- ---- Workflow Audit History ----

CREATE TABLE result_status_history (
    id               BIGSERIAL PRIMARY KEY,
    tenant_id        BIGINT NOT NULL,
    entity_type      VARCHAR(50) NOT NULL, -- MARK_SHEET, GRADE_CARD
    entity_id        BIGINT NOT NULL,
    previous_status  VARCHAR(50),
    new_status       VARCHAR(50) NOT NULL,
    changed_by       VARCHAR(120) NOT NULL,
    comments         VARCHAR(500),
    changed_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_rsh_entity ON result_status_history (tenant_id, entity_type, entity_id);

