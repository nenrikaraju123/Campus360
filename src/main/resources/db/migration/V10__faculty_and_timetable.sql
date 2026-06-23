-- ============================================================
-- V10: Faculty profiles, timetable engine, academic calendar
-- ============================================================

-- ---- Faculty ----

DROP TABLE IF EXISTS faculty_profiles CASCADE;

CREATE TABLE faculty_profiles (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT       NOT NULL,
    user_id         BIGINT       NOT NULL,
    employee_code   VARCHAR(40)  NOT NULL,
    first_name      VARCHAR(100) NOT NULL,
    last_name       VARCHAR(100),
    email           VARCHAR(200) NOT NULL,
    phone           VARCHAR(20),
    department_id   BIGINT,
    designation     VARCHAR(100),
    qualification   VARCHAR(200),
    employment_type VARCHAR(30)  DEFAULT 'FULL_TIME',
    joining_date    DATE,
    status          VARCHAR(20)  DEFAULT 'ACTIVE',
    created_at      TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    version         INT          DEFAULT 0,
    CONSTRAINT uq_faculty_employee_code UNIQUE (tenant_id, employee_code),
    CONSTRAINT uq_faculty_user UNIQUE (tenant_id, user_id)
);
CREATE INDEX idx_faculty_tenant ON faculty_profiles (tenant_id);
CREATE INDEX idx_faculty_dept   ON faculty_profiles (tenant_id, department_id);
CREATE INDEX idx_faculty_status ON faculty_profiles (tenant_id, status);

CREATE TABLE faculty_department_assignments (
    id            BIGSERIAL PRIMARY KEY,
    tenant_id     BIGINT  NOT NULL,
    faculty_id    BIGINT  NOT NULL,
    department_id BIGINT  NOT NULL,
    is_primary    BOOLEAN DEFAULT FALSE,
    assigned_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_fac_dept UNIQUE (tenant_id, faculty_id, department_id)
);
CREATE INDEX idx_fda_tenant ON faculty_department_assignments (tenant_id);

CREATE TABLE faculty_course_assignments (
    id          BIGSERIAL PRIMARY KEY,
    tenant_id   BIGINT  NOT NULL,
    faculty_id  BIGINT  NOT NULL,
    section_id  BIGINT  NOT NULL,
    course_id   BIGINT  NOT NULL,
    term_id     BIGINT,
    academic_year VARCHAR(20),
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status      VARCHAR(20) DEFAULT 'ACTIVE',
    CONSTRAINT uq_fca UNIQUE (tenant_id, faculty_id, section_id, course_id)
);
CREATE INDEX idx_fca_tenant   ON faculty_course_assignments (tenant_id);
CREATE INDEX idx_fca_faculty  ON faculty_course_assignments (tenant_id, faculty_id);
CREATE INDEX idx_fca_section  ON faculty_course_assignments (tenant_id, section_id);

CREATE TABLE faculty_workload_snapshots (
    id                  BIGSERIAL PRIMARY KEY,
    tenant_id           BIGINT NOT NULL,
    faculty_id          BIGINT NOT NULL,
    term_id             BIGINT,
    total_sections      INT    DEFAULT 0,
    total_courses       INT    DEFAULT 0,
    total_hours_per_week INT   DEFAULT 0,
    snapshot_date       DATE   NOT NULL,
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_fws_tenant ON faculty_workload_snapshots (tenant_id, faculty_id);

-- ---- Rooms ----

CREATE TABLE rooms (
    id          BIGSERIAL PRIMARY KEY,
    tenant_id   BIGINT      NOT NULL,
    room_code   VARCHAR(30) NOT NULL,
    name        VARCHAR(100),
    building    VARCHAR(100),
    floor       VARCHAR(20),
    capacity    INT         DEFAULT 0,
    room_type   VARCHAR(30) DEFAULT 'CLASSROOM',
    is_active   BOOLEAN     DEFAULT TRUE,
    created_at  TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_room_code UNIQUE (tenant_id, room_code)
);
CREATE INDEX idx_rooms_tenant ON rooms (tenant_id);

-- ---- Time Slots ----

CREATE TABLE time_slots (
    id          BIGSERIAL PRIMARY KEY,
    tenant_id   BIGINT      NOT NULL,
    day_of_week VARCHAR(10) NOT NULL,
    start_time  TIME        NOT NULL,
    end_time    TIME        NOT NULL,
    slot_label  VARCHAR(50),
    is_break    BOOLEAN     DEFAULT FALSE,
    display_order INT       DEFAULT 0,
    CONSTRAINT uq_timeslot UNIQUE (tenant_id, day_of_week, start_time, end_time)
);
CREATE INDEX idx_ts_tenant ON time_slots (tenant_id);

-- ---- Timetable ----

CREATE TABLE timetable_templates (
    id            BIGSERIAL PRIMARY KEY,
    tenant_id     BIGINT      NOT NULL,
    name          VARCHAR(100) NOT NULL,
    term_id       BIGINT,
    academic_year VARCHAR(20),
    status        VARCHAR(20) DEFAULT 'DRAFT',
    published_at  TIMESTAMP,
    published_by  BIGINT,
    created_at    TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP   DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_tt_template_tenant ON timetable_templates (tenant_id);

CREATE TABLE timetable_entries (
    id           BIGSERIAL PRIMARY KEY,
    tenant_id    BIGINT NOT NULL,
    template_id  BIGINT NOT NULL,
    section_id   BIGINT NOT NULL,
    course_id    BIGINT NOT NULL,
    faculty_id   BIGINT,
    room_id      BIGINT,
    time_slot_id BIGINT NOT NULL,
    day_of_week  VARCHAR(10) NOT NULL,
    entry_type   VARCHAR(20) DEFAULT 'LECTURE',
    created_at   TIMESTAMP   DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_tte_tenant    ON timetable_entries (tenant_id);
CREATE INDEX idx_tte_template  ON timetable_entries (tenant_id, template_id);
CREATE INDEX idx_tte_section   ON timetable_entries (tenant_id, section_id);
CREATE INDEX idx_tte_faculty   ON timetable_entries (tenant_id, faculty_id);
CREATE INDEX idx_tte_room      ON timetable_entries (tenant_id, room_id, day_of_week, time_slot_id);

CREATE TABLE timetable_conflicts (
    id            BIGSERIAL PRIMARY KEY,
    tenant_id     BIGINT NOT NULL,
    template_id   BIGINT NOT NULL,
    conflict_type VARCHAR(40) NOT NULL,
    entry_a_id    BIGINT NOT NULL,
    entry_b_id    BIGINT NOT NULL,
    description   VARCHAR(500),
    resolved      BOOLEAN DEFAULT FALSE,
    detected_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_ttc_template ON timetable_conflicts (tenant_id, template_id);

-- ---- Academic Calendar ----

CREATE TABLE academic_calendar_events (
    id            BIGSERIAL PRIMARY KEY,
    tenant_id     BIGINT       NOT NULL,
    title         VARCHAR(200) NOT NULL,
    description   VARCHAR(1000),
    event_type    VARCHAR(30)  NOT NULL,
    start_date    DATE         NOT NULL,
    end_date      DATE,
    scope         VARCHAR(30)  DEFAULT 'INSTITUTION',
    scope_id      BIGINT,
    is_all_day    BOOLEAN      DEFAULT TRUE,
    created_by    VARCHAR(120),
    created_at    TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_ace_tenant ON academic_calendar_events (tenant_id);
CREATE INDEX idx_ace_dates  ON academic_calendar_events (tenant_id, start_date, end_date);
CREATE INDEX idx_ace_type   ON academic_calendar_events (tenant_id, event_type);

CREATE TABLE academic_holidays (
    id            BIGSERIAL PRIMARY KEY,
    tenant_id     BIGINT      NOT NULL,
    holiday_date  DATE        NOT NULL,
    name          VARCHAR(100) NOT NULL,
    holiday_type  VARCHAR(30) DEFAULT 'GENERAL',
    is_optional   BOOLEAN     DEFAULT FALSE,
    created_at    TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_holiday UNIQUE (tenant_id, holiday_date, name)
);
CREATE INDEX idx_ah_tenant ON academic_holidays (tenant_id);
CREATE INDEX idx_ah_date   ON academic_holidays (tenant_id, holiday_date);

-- ---- Seed NumberingService key for FACULTY ----

INSERT INTO number_sequences (tenant_id, sequence_key, prefix, next_value, padding, updated_at)
SELECT i.id, 'FACULTY', 'FAC-', 1, 4, CURRENT_TIMESTAMP
FROM institutions i
WHERE NOT EXISTS (
    SELECT 1 FROM number_sequences ns
    WHERE ns.tenant_id = i.id AND ns.sequence_key = 'FACULTY'
);
