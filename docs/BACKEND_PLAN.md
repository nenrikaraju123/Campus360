# Campus360 — Enterprise Academic, Student & Placement Platform
### Complete Backend Engineering Plan (Spring Boot)

> Evolving the existing *EduTrack Insight* school project into **Campus360**: a multi-tenant,
> API-first backend that runs the full academic lifecycle of colleges and universities —
> admissions → academics → examinations → student life → placements → alumni.

---

## 0. Executive Summary

**What we are building:** A backend platform (REST + events) that any college or university can
onboard as a tenant. It manages the *entire* student journey and institutional operations, with a
flagship **Placement & Career** module that most ERPs treat as an afterthought.

**Architectural stance:** Start as a **modular monolith** (one deployable, clean module
boundaries), designed so individual modules (Placement, Notifications, Reporting) can be peeled
off into microservices later **without rewrites**. This gives a small team enterprise structure
without premature distributed-systems pain.

**Key principles**
1. **API-first** — every capability is a documented REST endpoint (OpenAPI). Thymeleaf UI is dropped; frontends (web/mobile) are consumers.
2. **Multi-tenant** — one platform, many institutions, isolated data.
3. **Secure by default** — Spring Security + JWT, RBAC, no plaintext secrets, audited.
4. **Domain-driven** — packages model real university concepts (Program, Course, Section, Term, CreditHour), not flat tables.
5. **Event-driven inside** — modules talk via domain events (Spring `ApplicationEventPublisher` → later Kafka) to stay decoupled.

---

## 1. Migration Reality Check (current → target)

| Area | Current (EduTrack) | Target (Campus360) |
|---|---|---|
| Style | Thymeleaf MVC monolith | API-first modular monolith (REST/JSON) |
| Auth | DOB-as-password, plaintext, session | Spring Security, BCrypt, JWT access+refresh, RBAC |
| Data model | Flat (`std`, subjects as columns) | Normalized: Program/Course/Section/Term/Enrollment |
| Persistence | `ddl-auto=create` (wipes data!) | `validate` + **Flyway** migrations |
| Secrets | **Committed in repo** 🔴 | Env vars / Vault, nothing in git |
| Tenancy | None | Tenant-aware everywhere |
| Scope | School (one class) | University (programs, semesters, credits, placements) |

**Verdict:** Treat the current code as a **reference for domain intent**, not a base to extend.
We greenfield the backend with clean architecture and port over the *ideas* (attendance,
marks, lectures, analytics) into a proper model. The existing repo's `pom.xml` and package
naming give us a starting Spring Boot skeleton.

### 1.1 Immediate fixes (do today, regardless of the big plan)
- [ ] Rotate the leaked Aiven DB password and Gmail app password — assume both are compromised.
- [ ] Remove secrets from `application.properties`; use `${ENV_VAR}` placeholders.
- [ ] Add `.gitignore` for `application-*.properties`, `*.env`, `/target`, `.idea`.
- [ ] Change `ddl-auto` to `validate` and introduce Flyway.

---

## 2. Stakeholders & Actor Map

The platform serves **9 primary actors**. Every use case below maps to one or more.

1. **Super Admin** (platform operator) — onboards institutions, manages tenants, billing, global config.
2. **Institution Admin** (registrar/principal office) — academic calendar, programs, fees, structure.
3. **HOD / Department Coordinator** — manages a department's courses, faculty, timetable.
4. **Faculty / Professor** — teaches sections, marks attendance, grades, uploads material.
5. **Student** — enrolls, attends, submits, views grades, applies to placements.
6. **Placement Officer / TPO** — manages recruiters, drives, eligibility, offers.
7. **Recruiter / Company HR** (external) — posts jobs, schedules interviews, makes offers.
8. **Parent / Guardian** — read-only visibility into ward's attendance, grades, fees.
9. **Alumni** — mentorship, referrals, networking, event participation.

Plus system actors: **Notification engine**, **Scheduler**, **Reporting/Analytics**.

---

## 3. Complete Use-Case Catalog

Organized by bounded context. Each context becomes a Java module/package.

### 3.1 Identity & Access (IAM)
- Register/onboard institution (tenant provisioning).
- User invite & self-registration with email verification.
- Login (JWT access + refresh), logout, token refresh, session revocation.
- Password reset (port existing flow), forced reset, password policy.
- Multi-factor auth (TOTP) for admins/staff.
- Role & permission management (RBAC), role assignment per tenant.
- Single Sign-On readiness (SAML/OAuth2 — Google Workspace, Microsoft) for later.
- Audit log of security-sensitive actions.

### 3.2 Institution & Academic Structure
- Manage institution profile, campuses, departments.
- Define **Programs** (B.Tech CSE, MBA, etc.) with duration & total credits.
- Define **Courses/Subjects** with credit hours, type (core/elective/lab), prerequisites.
- Build **Curriculum/Program Plan** (which courses in which semester).
- Define **Academic Terms** (semesters/years) with start/end, add-drop windows.
- Create **Sections** (a course offering in a term, taught by faculty, with capacity).
- Academic calendar & holiday management.

### 3.3 Admissions & Enrollment
- Application intake (prospective student applies to a program).
- Application review, shortlist, admit/reject workflow.
- Convert admitted applicant → enrolled student (generate roll/registration number).
- **Course registration** per term (student picks sections within rules: credit limits, prerequisites, clashes).
- Add/drop within window; waitlists when section is full.
- Section roster management for faculty.

### 3.4 Teaching & Learning (LMS-lite)
- Faculty uploads lectures, notes, videos, slides per course (port existing lecture/file features).
- Reading lists, syllabus publication.
- Announcements per section/course/department.
- Discussion threads / Q&A per course (optional phase 2).

### 3.5 Attendance
- Mark attendance per section per class meeting (single & bulk).
- Self/QR/biometric-ingest friendly API (record source-agnostic).
- Attendance % computation, low-attendance detection & auto-alerts (port + improve existing).
- Leave/medical exemption requests & approval.
- Parent/student attendance views.

### 3.6 Assessments & Examinations
- Create assessments (quiz, assignment, mid, end-sem) with max marks & weightage (generalize the current hardcoded-subjects model).
- Assignment submission (file upload), due dates, late penalties.
- Grade entry per assessment; rubrics (phase 2).
- **Weighted grade computation** → letter grade & **grade points** per course.
- **GPA / CGPA / SGPA** computation per term and cumulative.
- Transcript generation (official/unofficial).
- Re-evaluation / grievance workflow.
- Exam scheduling & hall/seat allocation (phase 2).

### 3.7 Fees & Finance (lightweight)
- Fee structure per program/term; scholarships & waivers.
- Invoice generation, payment recording, due reminders.
- Payment-gateway-ready API (Razorpay/Stripe webhooks) — pluggable.
- Fee/transcript holds blocking registration when dues exist.

### 3.8 🌟 Placement & Career (flagship module)
- Maintain **student career profile**: resume, skills, certifications, projects, CGPA, backlogs.
- Recruiter onboarding & company directory.
- **Job/Internship posting** with eligibility rules (min CGPA, branch, backlog cap, batch year).
- **Automatic eligibility filtering** — system computes who can apply per posting.
- Student applies; TPO approves/forwards applications.
- **Placement drive** lifecycle: rounds (online test → GD → tech → HR), per-round shortlists.
- Interview scheduling, slot booking, panel assignment.
- **Offer management**: offer letters, CTC, accept/decline, multiple-offer policy enforcement.
- **One-offer / dream-company policy engine** (configurable per institution).
- Placement statistics: placed %, highest/median/average CTC, branch-wise, recruiter-wise, YoY.
- Alumni referrals & mentorship matching.
- Mock interviews / readiness scoring (creative — see §10).

### 3.9 Student Life & Services
- Hostel/room allocation (optional).
- Library catalog & issue/return (optional, or integrate).
- Grievance & support tickets.
- Certificate/document requests (bonafide, transcripts) with approval workflow.
- Events, clubs, and participation tracking.

### 3.10 Notifications & Communication
- Email (port existing), SMS, push, in-app — unified API.
- Templated, multi-channel, per-event (low attendance, grade published, new job posting, interview scheduled).
- User notification preferences & digests.

### 3.11 Analytics, Reporting & Dashboards
- Role-specific dashboards (admin/HOD/faculty/student/TPO) — generalize current `AnalyticsController`.
- At-risk student detection (low attendance + falling grades) — extend existing `StudentRisk`.
- Department/program performance reports.
- Placement analytics & NAAC/NBA/accreditation report exports.
- Scheduled report generation & export (PDF/Excel/CSV).

---

## 4. Target Architecture

### 4.1 Style: Modular Monolith (microservice-ready)
```
campus360-backend (single Spring Boot app, single deployable)
└── com.campus360
    ├── platform        // cross-cutting: security, tenancy, audit, error, config
    ├── iam             // users, roles, auth, tokens
    ├── institution     // tenant, departments, programs, courses, terms, sections
    ├── admission       // applications, enrollment
    ├── academics       // registration, attendance, assessments, grades, gpa
    ├── learning        // lectures, materials, announcements
    ├── finance         // fees, invoices, payments
    ├── placement       // companies, jobs, drives, applications, offers
    ├── studentlife     // grievances, documents, events
    ├── notification    // multi-channel dispatch + templates
    ├── analytics       // dashboards, reports, risk detection
    └── shared          // common DTO base, value objects, events, utils
```
**Module rules**
- Each module exposes a **public API package** (`...module.api`) and hides internals (`...module.internal`).
- Cross-module calls go through the public API interface **or** domain events — never reach into another module's repositories.
- Enforce boundaries with **Spring Modulith** (`spring-modulith` verifies no illegal package deps at test time).

### 4.2 Layering within each module
```
controller (REST)  →  service (use cases, @Transactional)  →  domain (entities, rules)
                                          ↓
                                   repository (Spring Data JPA)
DTOs (request/response) ↔ MapStruct ↔ entities.   Validation via Jakarta Bean Validation.
```

### 4.3 Why this over full microservices now
- One team, one DB transaction boundary, vastly simpler ops.
- Clean module seams mean Placement or Notification can later become its own service when load/team justifies it.
- Domain events today = in-process; swap publisher to Kafka tomorrow with no domain change.

---

## 5. Technology Stack

| Concern | Choice | Notes |
|---|---|---|
| Language/Runtime | **Java 21 (LTS)** | upgrade from 17; records, pattern matching, virtual threads |
| Framework | **Spring Boot 3.3+** | keep |
| Web | Spring Web (REST) | drop Thymeleaf as primary UI |
| Persistence | Spring Data JPA + Hibernate | + **QueryDSL** for dynamic eligibility queries |
| DB | **PostgreSQL** (prod), H2 (tests) | Postgres > MySQL for JSONB, partial indexes, rich types |
| Migrations | **Flyway** | versioned SQL, no more `ddl-auto=create` |
| Security | Spring Security 6 + JWT | BCrypt, RBAC, method security |
| Mapping | **MapStruct** | entity↔DTO |
| Validation | Jakarta Bean Validation | `@Valid` on all inputs |
| Module boundaries | **Spring Modulith** | architecture tests |
| API docs | **springdoc-openapi** | Swagger UI |
| Caching | Spring Cache + **Redis** | sessions, hot reads, rate limiting |
| Async/Events | Spring events → **Kafka** (phase 3) | decoupling |
| Search | **OpenSearch/Elasticsearch** (phase 3) | resume/job search |
| Object storage | **S3-compatible (MinIO)** | lectures, resumes, docs (not DB blobs) |
| Background jobs | Spring `@Scheduled` + **Quartz** | reminders, GPA recompute, digests |
| Reporting | Apache POI / JasperReports | Excel/PDF exports |
| Observability | Actuator + **Micrometer → Prometheus + Grafana**, Zipkin tracing | |
| Logging | SLF4J + Logback, JSON logs, correlation IDs | |
| Testing | JUnit 5, Mockito, **Testcontainers**, REST Assured | real Postgres in tests |
| Build | Maven (keep) or Gradle | multi-module Maven |
| Containerization | Docker + docker-compose; K8s-ready | |
| CI/CD | GitHub Actions | build, test, scan, image |

---

## 5b. Tenant Onboarding & Authentication (implemented)

A real multi-tenant SaaS never lets a stranger self-activate a tenant. Campus360 uses a
**request → platform-admin approval → provisioning** lifecycle with **tenant-scoped logins**.

**Lifecycle**
```
 (public)            (SUPER_ADMIN)                         (institution)
 submit request  →   review queue   →  approve ─┬─ provision Institution (ACTIVE)
 (PENDING)           reject ─ notify           └─ create first INSTITUTION_ADMIN
                                                    (temp password, must-change-on-first-login)
```

**Endpoints**
- Public: `POST /api/v1/registrations` — submit onboarding request (PENDING; no active tenant yet).
- Platform (`SUPER_ADMIN`, `/api/v1/platform/**`):
  `GET /registrations[?status=]`, `GET /registrations/{id}`,
  `POST /registrations/{id}/approve` (→ provisions + returns one-time temp password),
  `POST /registrations/{id}/reject`,
  `POST /institutions` (direct provision), `GET /institutions`,
  `POST /institutions/{id}/suspend`, `POST /institutions/{id}/activate`.
- Auth: `POST /api/v1/auth/login` (**tenant-scoped**: `tenantCode + email + password`; platform admin
  omits `tenantCode`), `POST /auth/refresh`, `POST /auth/logout`,
  `POST /auth/change-password` (first-login rotation).

**Design decisions (the senior-engineer calls)**
- **Email is unique per tenant, not globally** (`uq(tenant_id,email)`), so the same person can be an
  admin at two institutions. Login therefore *must* be tenant-scoped — the tenant code disambiguates.
- **Platform accounts** have `tenant_id = NULL` and authenticate without a tenant code.
- **Suspended tenants** are rejected at both login and refresh — a suspended institution's users
  lose access within one access-token lifetime (15 min).
- **First admin password** is system-generated, delivered by (best-effort) email, and flagged
  `must_change_password`; the temp password is also returned to the approving admin so onboarding is
  never blocked by mail delivery.
- **Indistinguishable auth failures** (bad tenant, bad email, bad password all return the same error)
  to avoid user/tenant enumeration.

## 6. Multi-Tenancy Design

**Model:** Shared database, **shared schema with `tenant_id` discriminator** (simplest at start;
can graduate to schema-per-tenant for large clients).

- Every tenant-owned table has `tenant_id` (FK to `institution`).
- A **Hibernate filter** + `TenantContext` (ThreadLocal, set by a servlet filter from JWT claim) auto-scopes every query.
- Super Admin endpoints bypass the filter explicitly.
- Tenant-aware unique constraints (e.g., registration number unique *within* a tenant).
- Liquibase/Flyway baseline shared; tenant data isolated by row.

---

## 7. Security Design

- **AuthN:** username/email + BCrypt password → JWT **access token** (15 min) + **refresh token** (7 days, rotating, stored hashed). Refresh-token reuse detection.
- **AuthZ:** RBAC with fine-grained permissions. Roles: `SUPER_ADMIN, INSTITUTION_ADMIN, HOD, FACULTY, STUDENT, PLACEMENT_OFFICER, RECRUITER, PARENT, ALUMNI`. Method-level `@PreAuthorize`.
- **MFA:** TOTP for privileged roles.
- **Secrets:** environment variables / Spring Cloud Config / Vault. Nothing in git. `.env.example` documents required vars.
- **Transport:** HTTPS only, HSTS, secure cookies for refresh token.
- **Input:** Bean Validation everywhere; centralized `@ControllerAdvice` error handler with RFC 7807 *Problem Details*.
- **Rate limiting** (Bucket4j/Redis) on auth & public endpoints.
- **Audit trail:** who/what/when on sensitive mutations (grades, offers, money).
- **OWASP:** dependency scanning (OWASP Dependency-Check / Dependabot), no SQL string concat (JPA/QueryDSL).

---

## 8. Core Domain Model (key entities)

> Normalized to replace the current flat school model. Subjects are no longer columns —
> they are first-class `Course` rows, and marks reference assessments.

**Institution context**
- `Institution(id, name, code, type, address, ...)` ← tenant root
- `Department(id, institution_id, name, code, hodUserId)`
- `Program(id, department_id, name, level, durationTerms, totalCredits)`
- `Course(id, department_id, code, title, creditHours, type, prerequisites[])`
- `CurriculumItem(program_id, course_id, term_number, mandatory)`
- `AcademicTerm(id, institution_id, name, startDate, endDate, addDropEnd, status)`
- `Section(id, course_id, term_id, facultyUserId, capacity, schedule)`

**People & enrollment**
- `User(id, tenant_id, email, passwordHash, status, mfaSecret)` + `Role`, `Permission`
- `StudentProfile(userId, programId, rollNumber, batchYear, admissionDate, currentTerm)`
- `FacultyProfile(userId, departmentId, designation, specialization)`
- `Enrollment(id, studentId, sectionId, termId, status[ENROLLED/DROPPED/WAITLISTED], grade, gradePoints)`

**Academics**
- `ClassMeeting(id, sectionId, date, topic)`
- `AttendanceRecord(id, enrollmentId, classMeetingId, status[PRESENT/ABSENT/LATE/EXCUSED], source)`
- `Assessment(id, sectionId, title, type, maxMarks, weightagePct, dueDate)`
- `Submission(id, assessmentId, studentId, fileRef, submittedAt, isLate)`
- `Mark(id, assessmentId, enrollmentId, score, gradedBy, gradedAt)`
- Grade & GPA are **computed** (service), persisted as snapshots on `Enrollment` + `TermResult(studentId, termId, sgpa, cgpa, credits)`.

**Placement context**
- `Company(id, name, sector, tier, recruiterUserIds[])`
- `JobPosting(id, companyId, title, type[FT/INTERN], ctc, location, eligibility{minCgpa, branches[], maxBacklogs, batchYear}, status)`
- `Application(id, postingId, studentId, status[APPLIED/SHORTLISTED/REJECTED/OFFERED], appliedAt)`
- `Drive(id, postingId, rounds[])`, `DriveRound(id, driveId, type, sequence, scheduledAt)`
- `RoundResult(id, roundId, applicationId, result, feedback)`
- `Offer(id, applicationId, ctc, joiningDate, status[EXTENDED/ACCEPTED/DECLINED], policyFlags)`
- `CareerProfile(studentId, resumeRef, skills[], certifications[], projects[], readinessScore)`

All money as `BigDecimal`, all timestamps `Instant` UTC, all enums typed (not strings).

---

## 9. API Design Conventions

- Base: `/api/v1/...`, JSON, plural nouns. Example resources:
  - `POST /api/v1/auth/login` · `POST /api/v1/auth/refresh`
  - `GET  /api/v1/students/{id}/transcript`
  - `POST /api/v1/sections/{id}/attendance` (bulk mark)
  - `GET  /api/v1/placements/postings/{id}/eligible-students`
  - `POST /api/v1/placements/postings/{id}/applications`
- Pagination: `?page=&size=&sort=`; responses wrap `{ data, page, totalElements }`.
- Errors: RFC 7807 Problem Details (`type, title, status, detail, traceId`).
- Versioned via URI; breaking changes → `/v2`.
- Idempotency keys on offer/payment-creating endpoints.
- Every endpoint documented in OpenAPI with examples & required roles.

---

## 10. Creative / Differentiating Features (the "uniqueness")

These set Campus360 apart from generic college ERPs:

1. **Placement Readiness Score** — composite metric per student (CGPA + skills coverage + mock-interview performance + certifications + attendance) surfaced to TPO and student, with "what to improve" hints. Computed by a rules engine; AI-assist optional.
2. **Eligibility Engine as data, not code** — eligibility rules stored as structured JSON criteria, evaluated by a QueryDSL predicate builder, so TPOs define new rules per drive without code changes.
3. **At-Risk Early Warning** — extends the existing `StudentRisk` concept: combines attendance trend + grade trajectory + fee holds → ranked watchlist + auto-notify mentor/parent.
4. **Smart Timetable / Clash Detection** — registration rejects section combos that clash on schedule or violate prerequisites/credit caps, with suggested alternatives.
5. **Unified Notification Bus** — one event (`GradePublished`, `JobPosted`, `LowAttendance`) fans out to email/SMS/push/in-app per user preference and tenant config.
6. **Alumni Referral Graph** — connect students to alumni at target companies for referrals & mentorship; track referral → interview → offer conversion.
7. **Outcome-Based Accreditation Exports** — generate NAAC/NBA-style attainment reports (CO/PO mapping) directly from grade data — a real pain point for Indian institutions.
8. **Digital Document Vault** — verifiable, signed PDFs (transcripts, bonafide, offer letters) with a public verification URL + QR.
9. **AI Career Assistant (optional, phase 4)** — resume feedback, mock-interview Q&A, and job-fit explanations using an LLM (Claude) behind a service interface, so it's pluggable and not core-coupled.

---

## 11. Cross-Cutting Concerns

- **Config:** profile-based (`application-dev/test/prod.yml`), externalized secrets, Spring Cloud Config-ready.
- **Error handling:** global `@RestControllerAdvice`, typed exceptions, Problem Details, no stack traces leaked.
- **Audit:** JPA `@CreatedBy/@CreatedDate/@LastModifiedBy` + dedicated audit log for sensitive ops.
- **Soft deletes** where history matters (Hibernate `@SQLDelete`/`@Where`).
- **File storage:** abstract `StorageService` interface (S3/MinIO impl), never store large blobs in DB.
- **Idempotency & concurrency:** optimistic locking (`@Version`) on grades, offers, seats.
- **Observability:** health/readiness probes, metrics, distributed tracing, structured logs with correlation ID.
- **i18n** ready (message bundles) for multi-region institutions.

---

## 12. Phased Delivery Roadmap

> Each phase ends with a deployable, demoable backend. Build vertically (full slice) not horizontally.

### Phase 0 — Foundation & Safety (Week 1–2)
- New multi-module Maven project, Spring Boot 3.3 / Java 21.
- Postgres + Flyway baseline; Docker Compose for local (Postgres, Redis, MinIO, Mailhog).
- Rotate leaked secrets; externalize config; `.gitignore`.
- Global error handling, OpenAPI, base entity/auditing, CI pipeline.

### Phase 1 — IAM + Institution Core (Week 3–5)
- Tenancy, users, roles, JWT auth, password reset (port existing), MFA for admins.
- Institution/Department/Program/Course/Term/Section CRUD.
- **Milestone:** an admin can model a real university and create logins.

### Phase 2 — Academics Engine (Week 6–9)
- Enrollment & course registration with clash/prereq/credit rules.
- Attendance (bulk, %, low-attendance alerts) — port & generalize.
- Assessments, submissions, marks, weighted grades, **GPA/CGPA**, transcripts.
- **Milestone:** full term run-through: register → attend → assess → grade → transcript.

### Phase 3 — Placement & Career (Week 10–13)
- Company/recruiter onboarding, job postings, eligibility engine.
- Applications, drives, rounds, interviews, offers, policies.
- Placement analytics dashboards + exports.
- **Milestone:** end-to-end placement drive with eligibility filtering and offers.

### Phase 4 — Engagement & Intelligence (Week 14+)
- Unified notifications (email/SMS/push), preferences, digests.
- Analytics dashboards per role, at-risk detection, readiness score.
- Finance-lite (fees/holds), document vault, alumni/referrals.
- Optional AI assistant, search (OpenSearch), event streaming (Kafka).

---

## 13. Testing & Quality Strategy
- **Unit:** services & domain rules (Mockito).
- **Integration:** repositories & controllers against **Testcontainers Postgres** (real DB).
- **Architecture:** Spring Modulith tests enforce module boundaries.
- **Contract/API:** REST Assured + OpenAPI validation.
- **Coverage gate** in CI (e.g., 70%+ on services), SpotBugs + Checkstyle, OWASP dependency scan.
- Seed/demo data loader for a fictitious university to demo & test against.

---

## 14. Repository & Project Layout (target)
```
campus360-backend/
├── pom.xml                      (parent, multi-module)
├── docker-compose.yml           (postgres, redis, minio, mailhog)
├── .env.example
├── modules/
│   ├── platform/  iam/  institution/  admission/  academics/
│   ├── learning/  finance/  placement/  studentlife/
│   ├── notification/  analytics/  shared/
├── app/                         (bootstraps & wires all modules → single jar)
├── db/migration/                (Flyway V1__, V2__ ...)
└── docs/  (this plan, ADRs, OpenAPI, ER diagrams)
```

---

## 15. Locked Decisions (confirmed 2026-06-20)
1. **Database:** ✅ **PostgreSQL** — migrate off MySQL. JSONB powers the eligibility engine and config-as-data.
2. **Tenancy:** ✅ **Multi-tenant** — one platform, many institutions, shared schema + `tenant_id` discriminator (§6).
3. **First module after core:** ✅ **Placement & Career** (§3.8, §8).
4. **API surface:** ✅ **API-only** — drop Thymeleaf, pure REST/JSON.
5. **Runtime:** ✅ **Java 21 (LTS)** + Spring Boot 3.3.
6. Build tool: **Maven** (multi-module) — keep, matches current project.

### 15.1 Adjusted build sequence (given Placement-first)
Placement eligibility needs a few academic facts (CGPA, active backlogs, branch, batch year).
So instead of building the full Academics engine first, we build a **thin student-academics
foundation** that Placement consumes, then deepen Academics later:

- **Phase 0** — Foundation & safety (unchanged): multi-module project, Postgres+Flyway, Docker Compose, secrets rotated/externalized, error handling, OpenAPI, CI.
- **Phase 1** — IAM + Institution core + multi-tenancy: users, roles, JWT, MFA; Institution/Department/Program/Course/Term/Section; **StudentProfile with CGPA/backlog/branch/batch fields** (manually settable for now).
- **Phase 2 (was Phase 3)** — **Placement & Career**: companies, recruiters, job postings, **JSONB eligibility engine**, applications, drives/rounds, interviews, offers, policy engine, placement analytics.
- **Phase 3 (was Phase 2)** — **Full Academics engine**: enrollment/registration, attendance, assessments, marks, **computed GPA/CGPA** that then *feeds* the Placement profile automatically (replacing manual CGPA entry).
- **Phase 4** — Notifications, analytics dashboards, readiness score, finance-lite, document vault, alumni/referrals, optional AI assistant.

---

*Next step after sign-off:* I scaffold **Phase 0 + Phase 1** (multi-module Maven, Postgres/Flyway,
Docker Compose, JWT security, multi-tenancy, IAM, institution core, student profile) — a running,
secure, documented backend — then move straight into the **Placement & Career** module.
