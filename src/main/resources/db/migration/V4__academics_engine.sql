-- ============================================================
-- Campus360 V4 : Academics engine, audit log, notifications,
--                fees, curriculum, password-reset, optimistic locking
-- ============================================================

-- ---- Optimistic locking (@Version) on critical entities ----
ALTER TABLE users ADD COLUMN version INT NOT NULL DEFAULT 0;
ALTER TABLE student_profiles ADD COLUMN version INT NOT NULL DEFAULT 0;
ALTER TABLE job_postings ADD COLUMN version INT NOT NULL DEFAULT 0;
ALTER TABLE applications ADD COLUMN version INT NOT NULL DEFAULT 0;
ALTER TABLE offers ADD COLUMN version INT NOT NULL DEFAULT 0;
ALTER TABLE institutions ADD COLUMN version INT NOT NULL DEFAULT 0;

-- ---- Password reset tokens ----
CREATE TABLE password_reset_tokens (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash  VARCHAR(120) NOT NULL UNIQUE,
    expires_at  TIMESTAMPTZ NOT NULL,
    used        BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_password_reset_user ON password_reset_tokens(user_id);

-- ---- Audit log (enterprise audit trail) ----
CREATE TABLE audit_log (
    id           BIGSERIAL PRIMARY KEY,
    tenant_id    BIGINT REFERENCES institutions(id),
    actor_id     BIGINT,
    actor_email  VARCHAR(180),
    action       VARCHAR(80) NOT NULL,
    entity_type  VARCHAR(80) NOT NULL,
    entity_id    BIGINT,
    detail       TEXT,
    ip_address   VARCHAR(45),
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_audit_log_tenant ON audit_log(tenant_id, created_at DESC);
CREATE INDEX idx_audit_log_actor ON audit_log(actor_id);
CREATE INDEX idx_audit_log_entity ON audit_log(entity_type, entity_id);

-- ---- Curriculum items (program plan) ----
CREATE TABLE curriculum_items (
    id          BIGSERIAL PRIMARY KEY,
    tenant_id   BIGINT NOT NULL REFERENCES institutions(id),
    program_id  BIGINT NOT NULL REFERENCES programs(id) ON DELETE CASCADE,
    course_id   BIGINT NOT NULL REFERENCES courses(id),
    term_number INT NOT NULL,
    mandatory   BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_curriculum_program_course UNIQUE (program_id, course_id)
);

-- ---- Enrollments (course registration) ----
CREATE TABLE enrollments (
    id          BIGSERIAL PRIMARY KEY,
    tenant_id   BIGINT NOT NULL REFERENCES institutions(id),
    student_id  BIGINT NOT NULL REFERENCES student_profiles(id),
    section_id  BIGINT NOT NULL REFERENCES sections(id),
    term_id     BIGINT NOT NULL REFERENCES academic_terms(id),
    status      VARCHAR(20) NOT NULL DEFAULT 'ENROLLED',   -- ENROLLED / DROPPED / WAITLISTED / COMPLETED
    grade       VARCHAR(5),                                 -- A+ / A / B+ / B / C / D / F / W / I
    grade_points NUMERIC(4,2),
    version     INT NOT NULL DEFAULT 0,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by  VARCHAR(120),
    updated_by  VARCHAR(120),
    CONSTRAINT uq_enrollment_student_section UNIQUE (student_id, section_id)
);
CREATE INDEX idx_enrollments_term ON enrollments(tenant_id, term_id);
CREATE INDEX idx_enrollments_student ON enrollments(student_id);

-- ---- Class meetings (individual class sessions) ----
CREATE TABLE class_meetings (
    id          BIGSERIAL PRIMARY KEY,
    tenant_id   BIGINT NOT NULL REFERENCES institutions(id),
    section_id  BIGINT NOT NULL REFERENCES sections(id),
    meeting_date DATE NOT NULL,
    topic       VARCHAR(300),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by  VARCHAR(120)
);
CREATE INDEX idx_meetings_section ON class_meetings(section_id, meeting_date);

-- ---- Attendance records ----
CREATE TABLE attendance_records (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT NOT NULL REFERENCES institutions(id),
    enrollment_id   BIGINT NOT NULL REFERENCES enrollments(id),
    meeting_id      BIGINT NOT NULL REFERENCES class_meetings(id),
    status          VARCHAR(20) NOT NULL DEFAULT 'PRESENT',   -- PRESENT / ABSENT / LATE / EXCUSED
    source          VARCHAR(30) DEFAULT 'MANUAL',             -- MANUAL / QR / BIOMETRIC
    marked_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    marked_by       VARCHAR(120),
    CONSTRAINT uq_attendance UNIQUE (enrollment_id, meeting_id)
);
CREATE INDEX idx_attendance_meeting ON attendance_records(meeting_id);

-- ---- Assessments ----
CREATE TABLE assessments (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT NOT NULL REFERENCES institutions(id),
    section_id      BIGINT NOT NULL REFERENCES sections(id),
    title           VARCHAR(200) NOT NULL,
    type            VARCHAR(30) NOT NULL DEFAULT 'ASSIGNMENT',  -- QUIZ / ASSIGNMENT / MIDTERM / ENDTERM / LAB / PROJECT
    max_marks       NUMERIC(6,2) NOT NULL,
    weightage_pct   NUMERIC(5,2) NOT NULL DEFAULT 100.0,
    due_date        TIMESTAMPTZ,
    instructions    VARCHAR(2000),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by      VARCHAR(120),
    updated_by      VARCHAR(120)
);
CREATE INDEX idx_assessments_section ON assessments(section_id);

-- ---- Submissions ----
CREATE TABLE submissions (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT NOT NULL REFERENCES institutions(id),
    assessment_id   BIGINT NOT NULL REFERENCES assessments(id),
    student_id      BIGINT NOT NULL REFERENCES student_profiles(id),
    file_ref        VARCHAR(500),
    content         TEXT,
    submitted_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    is_late         BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT uq_submission UNIQUE (assessment_id, student_id)
);

-- ---- Marks / Grades ----
CREATE TABLE marks (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT NOT NULL REFERENCES institutions(id),
    assessment_id   BIGINT NOT NULL REFERENCES assessments(id),
    enrollment_id   BIGINT NOT NULL REFERENCES enrollments(id),
    score           NUMERIC(6,2) NOT NULL,
    remarks         VARCHAR(500),
    graded_by       VARCHAR(120),
    graded_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_mark UNIQUE (assessment_id, enrollment_id)
);

-- ---- Term results (GPA/CGPA snapshots) ----
CREATE TABLE term_results (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT NOT NULL REFERENCES institutions(id),
    student_id      BIGINT NOT NULL REFERENCES student_profiles(id),
    term_id         BIGINT NOT NULL REFERENCES academic_terms(id),
    credits_earned  INT NOT NULL DEFAULT 0,
    credits_attempted INT NOT NULL DEFAULT 0,
    sgpa            NUMERIC(4,2) NOT NULL DEFAULT 0.0,
    cgpa            NUMERIC(4,2) NOT NULL DEFAULT 0.0,
    computed_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_term_result UNIQUE (student_id, term_id)
);

-- ---- Persistent notifications ----
CREATE TABLE notifications (
    id          BIGSERIAL PRIMARY KEY,
    tenant_id   BIGINT REFERENCES institutions(id),
    user_id     BIGINT REFERENCES users(id),
    type        VARCHAR(60) NOT NULL,
    title       VARCHAR(200) NOT NULL,
    message     VARCHAR(2000),
    channel     VARCHAR(20) NOT NULL DEFAULT 'IN_APP',  -- IN_APP / EMAIL / SMS / PUSH
    is_read     BOOLEAN NOT NULL DEFAULT FALSE,
    read_at     TIMESTAMPTZ,
    ref_type    VARCHAR(60),
    ref_id      BIGINT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_notifications_user ON notifications(user_id, is_read, created_at DESC);
CREATE INDEX idx_notifications_tenant ON notifications(tenant_id, created_at DESC);

-- ---- Notification preferences ----
CREATE TABLE notification_preferences (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    email_enabled   BOOLEAN NOT NULL DEFAULT TRUE,
    sms_enabled     BOOLEAN NOT NULL DEFAULT FALSE,
    push_enabled    BOOLEAN NOT NULL DEFAULT TRUE,
    in_app_enabled  BOOLEAN NOT NULL DEFAULT TRUE,
    digest_enabled  BOOLEAN NOT NULL DEFAULT FALSE,
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ---- Fee structures ----
CREATE TABLE fee_structures (
    id          BIGSERIAL PRIMARY KEY,
    tenant_id   BIGINT NOT NULL REFERENCES institutions(id),
    program_id  BIGINT REFERENCES programs(id),
    term_id     BIGINT REFERENCES academic_terms(id),
    name        VARCHAR(160) NOT NULL,
    amount      NUMERIC(12,2) NOT NULL,
    fee_type    VARCHAR(40) NOT NULL DEFAULT 'TUITION',  -- TUITION / HOSTEL / LAB / EXAM / MISC
    due_date    DATE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by  VARCHAR(120),
    updated_by  VARCHAR(120)
);

-- ---- Invoices ----
CREATE TABLE invoices (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT NOT NULL REFERENCES institutions(id),
    student_id      BIGINT NOT NULL REFERENCES student_profiles(id),
    fee_structure_id BIGINT NOT NULL REFERENCES fee_structures(id),
    amount          NUMERIC(12,2) NOT NULL,
    due_date        DATE,
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',  -- PENDING / PAID / OVERDUE / WAIVED / PARTIALLY_PAID
    paid_amount     NUMERIC(12,2) NOT NULL DEFAULT 0,
    invoice_number  VARCHAR(40) NOT NULL UNIQUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_invoices_student ON invoices(student_id, status);

-- ---- Payments ----
CREATE TABLE payments (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT NOT NULL REFERENCES institutions(id),
    invoice_id      BIGINT NOT NULL REFERENCES invoices(id),
    amount          NUMERIC(12,2) NOT NULL,
    payment_method  VARCHAR(40),      -- ONLINE / CASH / CHEQUE / UPI / BANK_TRANSFER
    transaction_ref VARCHAR(120),
    paid_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
    recorded_by     VARCHAR(120)
);

-- ---- Leave / exemption requests ----
CREATE TABLE leave_requests (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT NOT NULL REFERENCES institutions(id),
    student_id      BIGINT NOT NULL REFERENCES student_profiles(id),
    section_id      BIGINT REFERENCES sections(id),
    leave_type      VARCHAR(30) NOT NULL DEFAULT 'MEDICAL',  -- MEDICAL / PERSONAL / ACADEMIC / OTHER
    start_date      DATE NOT NULL,
    end_date        DATE NOT NULL,
    reason          VARCHAR(500),
    supporting_doc  VARCHAR(500),
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',  -- PENDING / APPROVED / REJECTED
    reviewed_by     VARCHAR(120),
    reviewed_at     TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_leave_requests_student ON leave_requests(student_id, status);

-- ---- Grievances / support tickets ----
CREATE TABLE grievances (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT NOT NULL REFERENCES institutions(id),
    user_id         BIGINT NOT NULL REFERENCES users(id),
    category        VARCHAR(40) NOT NULL DEFAULT 'GENERAL',  -- ACADEMIC / PLACEMENT / FEE / HOSTEL / GENERAL
    subject         VARCHAR(200) NOT NULL,
    description     VARCHAR(4000) NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'OPEN',     -- OPEN / IN_PROGRESS / RESOLVED / CLOSED
    priority        VARCHAR(10) NOT NULL DEFAULT 'MEDIUM',   -- LOW / MEDIUM / HIGH / CRITICAL
    assigned_to     BIGINT REFERENCES users(id),
    resolution      VARCHAR(2000),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_grievances_tenant ON grievances(tenant_id, status);

-- ---- Document requests ----
CREATE TABLE document_requests (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT NOT NULL REFERENCES institutions(id),
    student_id      BIGINT NOT NULL REFERENCES student_profiles(id),
    doc_type        VARCHAR(40) NOT NULL,    -- TRANSCRIPT / BONAFIDE / MIGRATION / CHARACTER / DEGREE
    purpose         VARCHAR(300),
    copies          INT NOT NULL DEFAULT 1,
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',  -- PENDING / PROCESSING / READY / COLLECTED / REJECTED
    document_ref    VARCHAR(500),
    reviewed_by     VARCHAR(120),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ---- Add skills/certifications columns to career_profiles ----
ALTER TABLE career_profiles ADD COLUMN IF NOT EXISTS projects TEXT;
