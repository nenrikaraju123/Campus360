# Campus360 Backend Technical Implementation Plan

This document converts `docs/ENTERPRISE_IMPLEMENTATION_PLAN.md` into backend engineering work that can be implemented one milestone at a time.

The current backend is already a Spring Boot modular monolith with these modules:

- `iam`
- `platform`
- `onboarding`
- `institution`
- `student`
- `academics`
- `finance`
- `placement`
- `studentlife`
- `notification`
- `analytics`
- `ai`
- `importer`

Keep this modular-monolith style. Do not split into microservices now. The product needs depth, correctness, and production readiness first.

## 1. Backend Implementation Rules

Every backend feature must follow these rules.

### Package Structure

Use this shape for every business module:

```text
com.campus360.<module>
  domain
  repository
  service
  web
  web.dto
```

Use additional subpackages only when the module becomes large:

```text
com.campus360.<module>
  workflow
  report
  event
  policy
```

### Controller Rules

- Controllers only handle HTTP mapping, request validation, and response status.
- Business rules must live in services.
- Every write request must use `@Valid`.
- Every protected endpoint must use `@PreAuthorize`.
- Every list endpoint must support pagination when the list can grow.
- Do not return raw stack traces or implementation exceptions.

### Service Rules

- Services own transactions.
- Use `@Transactional` on write workflows.
- Use `@Transactional(readOnly = true)` on read workflows.
- Every tenant-owned operation must call `TenantContext.requireTenantId()`.
- Every tenant-owned repository lookup must include `tenantId`.
- Every sensitive operation must write audit logs.
- Every workflow status change must create status history.

### Repository Rules

- Tenant-owned entities must have `tenant_id`.
- Repository methods must use `findByIdAndTenantId`, not only `findById`.
- Unique constraints must be tenant-aware.
- Add indexes for tenant, status, date, and common filters.

### Migration Rules

- Only Flyway migrations may change schema.
- Continue from the current migration number after `V4__academics_engine.sql`.
- Keep each migration focused by module.
- Never rely on Hibernate auto-DDL.

Recommended migration order:

```text
V5__platform_enterprise_foundation.sql
V6__permissions_and_user_security.sql
V7__document_storage.sql
V8__admissions_lifecycle.sql
V9__student_360_and_parent_portal.sql
V10__faculty_and_timetable.sql
V11__exams_results_and_grade_cards.sql
V12__finance_enterprise_depth.sql
V13__placement_crm_depth.sql
V14__communication_and_templates.sql
V15__workflow_service_desk.sql
V16__analytics_ai_governance.sql
V17__production_hardening_indexes.sql
```

Bulk import framework tables should be added in `V5__platform_enterprise_foundation.sql` because they are shared by students, courses, faculty, admissions, finance, and placements.

### API Rules

Use this pattern:

```text
/api/v1/<module>
/api/v1/<module>/{id}
/api/v1/<module>/{id}/actions/<action>
```

For workflow actions, prefer explicit action endpoints:

```text
POST /api/v1/admissions/applications/{id}/actions/approve
POST /api/v1/admissions/applications/{id}/actions/reject
POST /api/v1/finance/invoices/{id}/actions/waive
POST /api/v1/exams/results/{id}/actions/publish
```

### Status History Rule

For all important business workflows, create history tables:

- admission status history
- student lifecycle history
- invoice status history
- application pipeline history
- document request history
- grievance history
- approval history

History record minimum fields:

- `id`
- `tenant_id`
- `entity_type`
- `entity_id`
- `from_status`
- `to_status`
- `comment`
- `actor_id`
- `created_at`

### Test Rules

Minimum backend tests for every module:

- Controller authorization tests.
- Service validation tests.
- Tenant isolation tests.
- Repository query tests for major filters.
- Workflow status transition tests.

Use test naming like:

```text
AdmissionApplicationControllerTest
AdmissionApplicationServiceTest
AdmissionTenantIsolationTest
```

### Bulk Operation Rules

Real institutions do not add every student, course, faculty member, or invoice manually. Bulk workflows are mandatory.

All bulk operations must follow this enterprise pattern:

1. Upload file.
2. Parse file.
3. Validate every row.
4. Show validation summary.
5. Allow correction and re-upload.
6. Commit only valid rows, or commit all rows only when the selected mode requires full success.
7. Store row-level errors.
8. Create audit logs.
9. Send onboarding or workflow notifications where required.

Bulk import must never directly insert data without validation preview.

### Account Provisioning and Notification Rules

Whenever the system creates a login account for an institution admin, faculty member, student, parent, placement officer, HOD, or recruiter, the backend must:

- Create the IAM user with the correct tenant and role.
- Generate a temporary password or secure password setup token.
- Mark `must_change_password = true` where temporary password is used.
- Send a welcome email with institution name, tenant code, login URL, user role, and next steps.
- Avoid exposing password in logs.
- Audit account creation.
- Store notification delivery status.

For production, prefer password setup links over sending plain temporary passwords. Temporary passwords are acceptable only for local/dev/demo workflows.

## 2. Phase 0: Stabilization First

Do this before adding major features.

### 2.1 Configuration Hardening

Files to add or change:

```text
src/main/resources/application.yml
src/main/resources/application-dev.yml
src/main/resources/application-prod.yml
src/main/java/com/campus360/platform/config/CorsProperties.java
src/main/java/com/campus360/platform/config/AppSecurityProperties.java
```

Implementation:

- Move local-only defaults to `application-dev.yml`.
- Make production secrets mandatory through environment variables.
- Remove real mail usernames/passwords from committed config.
- Add explicit allowed origins configuration.
- Add production-safe server error settings.

Done when:

- App starts locally with dev defaults.
- Production profile fails fast if required secrets are missing.
- No committed real password exists in config.

### 2.2 Error Contract

Files to add or change:

```text
src/main/java/com/campus360/platform/error/ApiError.java
src/main/java/com/campus360/platform/error/GlobalExceptionHandler.java
src/main/java/com/campus360/platform/error/ValidationError.java
```

Response shape:

```json
{
  "timestamp": "2026-06-22T10:00:00Z",
  "status": 400,
  "code": "VALIDATION_FAILED",
  "message": "Request validation failed",
  "path": "/api/v1/students",
  "correlationId": "abc-123",
  "fieldErrors": [
    { "field": "email", "message": "must be a valid email" }
  ]
}
```

Done when:

- Validation errors, not found errors, access denied, duplicate data, and unexpected errors return consistent response.
- Correlation id is included.

### 2.3 Tenant Isolation Test Harness

Files to add:

```text
src/test/java/com/campus360/testsupport/TestTenantFactory.java
src/test/java/com/campus360/testsupport/TestUserFactory.java
src/test/java/com/campus360/testsupport/WithMockCampusUser.java
src/test/java/com/campus360/testsupport/TenantIsolationAssertions.java
```

Implementation:

- Create test helpers for two tenants.
- Create users for each role.
- Provide helper to assert tenant A cannot read tenant B data.

Done when:

- At least students, fees, academics, placements, notifications, and student life have tenant isolation tests.

### 2.4 Backend Test Baseline

Add tests for current core workflows:

```text
src/test/java/com/campus360/iam/AuthControllerTest.java
src/test/java/com/campus360/onboarding/PlatformOnboardingFlowTest.java
src/test/java/com/campus360/student/StudentControllerTest.java
src/test/java/com/campus360/academics/AttendanceControllerTest.java
src/test/java/com/campus360/finance/FeeControllerTest.java
src/test/java/com/campus360/placement/PlacementControllerTest.java
```

Done when:

- `mvn test` passes.
- Core role and tenant boundaries are covered.

## 3. Phase 1: Enterprise Platform Foundation

### 3.1 Fine-Grained Permissions

Current broad roles are useful, but enterprise clients need configurable permissions.

Migration:

```text
V6__permissions_and_user_security.sql
```

Tables:

```text
permissions
role_permissions
user_login_events
account_lock_events
```

Entities:

```text
iam.domain.Permission
iam.domain.UserLoginEvent
```

Repositories:

```text
iam.repository.PermissionRepository
iam.repository.UserLoginEventRepository
```

Services:

```text
iam.service.PermissionService
iam.service.LoginAuditService
```

APIs:

```text
GET  /api/v1/iam/permissions
GET  /api/v1/iam/roles/{roleId}/permissions
PUT  /api/v1/iam/roles/{roleId}/permissions
GET  /api/v1/iam/users/{userId}/login-history
POST /api/v1/iam/users/{userId}/actions/deactivate
POST /api/v1/iam/users/{userId}/actions/reactivate
POST /api/v1/iam/users/{userId}/actions/reset-password
```

Implementation order:

1. Add permission table and seed default permissions.
2. Link permissions to existing roles.
3. Add `PermissionEvaluator` or helper service.
4. Keep existing role checks working while adding permission checks gradually.
5. Add login event recording.
6. Add account lock/throttle policy.

Done when:

- Existing role behavior still works.
- Admin can view and update role permissions.
- Login history is recorded.
- Failed logins are audited without exposing whether email or tenant exists.

### 3.2 Tenant Plans and Tenant Settings

Migration:

```text
V5__platform_enterprise_foundation.sql
```

Tables:

```text
tenant_plans
tenant_subscriptions
tenant_settings
tenant_usage_snapshots
```

Entities:

```text
platform.subscription.TenantPlan
platform.subscription.TenantSubscription
platform.subscription.TenantUsageSnapshot
institution.domain.TenantSetting
```

Services:

```text
platform.subscription.SubscriptionService
platform.subscription.TenantUsageService
institution.service.TenantSettingsService
```

APIs:

```text
GET  /api/v1/platform/plans
POST /api/v1/platform/plans
PUT  /api/v1/platform/plans/{id}
GET  /api/v1/platform/institutions/{id}/subscription
PUT  /api/v1/platform/institutions/{id}/subscription
GET  /api/v1/platform/institutions/{id}/usage
GET  /api/v1/institution/settings
PUT  /api/v1/institution/settings
```

Settings to support first:

- logo URL
- academic year
- grading mode
- attendance minimum percentage
- fee due reminder days
- placement eligibility defaults
- notification preferences

Done when:

- Platform admin can assign a plan to a tenant.
- Institution admin can update tenant settings.
- Tenant status/plan can influence access in service layer later.

### 3.3 Audit Log Improvements

Current audit exists. Extend it.

Add filters:

```text
tenantId
actorId
module
action
entityType
entityId
fromDate
toDate
```

API:

```text
GET /api/v1/audit-logs
```

Implementation:

- Add specification-style query filtering.
- Return paginated response.
- Restrict platform admin to all tenants.
- Restrict institution admin to own tenant.

Done when:

- Audit logs are searchable and tenant-safe.

### 3.4 Numbering Service

Use database-backed counters for business numbers.

Migration:

```text
V5__platform_enterprise_foundation.sql
```

Table:

```text
number_sequences
```

Fields:

```text
id
tenant_id
sequence_key
prefix
next_value
padding
financial_year
updated_at
```

Service:

```text
platform.numbering.NumberingService
```

Initial keys:

- `STUDENT`
- `ADMISSION`
- `INVOICE`
- `RECEIPT`
- `CERTIFICATE`

API:

```text
GET /api/v1/institution/numbering
PUT /api/v1/institution/numbering/{key}
```

Done when:

- Invoice number generation no longer uses an in-memory counter.
- Student/admission numbers can use the same service.

### 3.5 Enterprise Bulk Import Framework

New package:

```text
com.campus360.importer
```

Migration:

```text
V5__platform_enterprise_foundation.sql
```

Tables:

```text
import_templates
import_jobs
import_job_rows
import_job_errors
import_job_commits
```

Entities:

```text
importer.domain.ImportTemplate
importer.domain.ImportJob
importer.domain.ImportJobRow
importer.domain.ImportJobError
importer.domain.ImportJobCommit
```

Repositories:

```text
ImportTemplateRepository
ImportJobRepository
ImportJobRowRepository
ImportJobErrorRepository
```

Services:

```text
ImportTemplateService
ImportParserService
ImportValidationService
ImportCommitService
ImportJobService
```

Strategy interface:

```java
public interface ImportHandler {
    String type();
    ImportValidationResult validateRow(Long tenantId, Map<String, String> row);
    ImportCommitResult commitRow(Long tenantId, Map<String, String> row, ImportCommitContext context);
}
```

Initial import types:

```text
STUDENTS
COURSES
DEPARTMENTS
PROGRAMS
SECTIONS
FACULTY
PARENTS
COMPANIES
FEE_ASSIGNMENTS
```

Job statuses:

```text
UPLOADED
PARSING
VALIDATING
VALIDATION_FAILED
READY_TO_COMMIT
COMMITTING
COMMITTED
PARTIALLY_COMMITTED
FAILED
CANCELLED
```

APIs:

```text
GET  /api/v1/imports/templates
GET  /api/v1/imports/templates/{type}/download
POST /api/v1/imports/jobs
GET  /api/v1/imports/jobs
GET  /api/v1/imports/jobs/{id}
GET  /api/v1/imports/jobs/{id}/rows
GET  /api/v1/imports/jobs/{id}/errors
POST /api/v1/imports/jobs/{id}/actions/validate
POST /api/v1/imports/jobs/{id}/actions/commit
POST /api/v1/imports/jobs/{id}/actions/cancel
```

Import job fields:

```text
id
tenant_id
type
original_file_name
storage_document_id
status
total_rows
valid_rows
invalid_rows
committed_rows
failed_rows
uploaded_by
committed_by
created_at
updated_at
committed_at
```

Validation capabilities:

- Required columns.
- Data type validation.
- Email and phone format validation.
- Duplicate row detection inside uploaded file.
- Duplicate existing data detection in tenant database.
- Reference lookup validation, such as program code, department code, section code, term code.
- Permission validation before commit.
- Maximum row limit by tenant plan.

Commit modes:

```text
ALL_OR_NOTHING
VALID_ROWS_ONLY
```

Rules:

- Large imports should run asynchronously.
- Import files should be stored through the document storage abstraction after that module exists.
- Before document storage exists, store only metadata and parsed rows in the database.
- Import commit must be idempotent; retrying a committed job must not duplicate records.
- Import commit must write audit logs per job and summary audit per module.
- Import commit must publish notification events when users are created.

Done when:

- Students and courses can use the same import engine.
- Import preview returns row-level errors.
- Commit can create records safely without duplicates.
- Import history is visible per tenant.

### 3.6 Account Invitation and Welcome Notification Service

New or extended package:

```text
com.campus360.iam.invitation
```

Migration:

```text
V6__permissions_and_user_security.sql
```

Tables:

```text
account_invitations
welcome_notification_jobs
```

Entities:

```text
iam.domain.AccountInvitation
iam.domain.WelcomeNotificationJob
```

Services:

```text
iam.service.AccountProvisioningService
iam.service.AccountInvitationService
notification.service.WelcomeNotificationService
```

APIs:

```text
POST /api/v1/iam/invitations
GET  /api/v1/iam/invitations
POST /api/v1/iam/invitations/{id}/actions/resend
POST /api/v1/iam/invitations/{id}/actions/revoke
POST /api/v1/iam/users/{userId}/actions/resend-welcome
```

Welcome email templates:

```text
WELCOME_INSTITUTION_ADMIN
WELCOME_HOD
WELCOME_FACULTY
WELCOME_STUDENT
WELCOME_PARENT
WELCOME_PLACEMENT_OFFICER
WELCOME_RECRUITER
PASSWORD_RESET
ACCOUNT_ACTIVATED
ACCOUNT_DEACTIVATED
```

Welcome email must include:

- Campus360 product name.
- Institution name.
- Tenant code.
- User name.
- Assigned role.
- Login URL.
- Temporary password or secure setup link.
- Password change instruction.
- Support/contact details configured by tenant.

Rules:

- Do not send welcome email until transaction commits successfully.
- Use application events or transactional event listeners.
- Store delivery status: `PENDING`, `SENT`, `FAILED`, `SKIPPED`.
- Allow resend by authorized admin.
- For bulk imports, send emails after successful commit, not during validation.
- For production, use password setup token by default.

Done when:

- Creating institution admin, faculty, student, parent, placement officer, or recruiter can trigger role-specific welcome email.
- Failed emails do not roll back successful account creation.
- Admin can see and resend failed welcome notifications.

## 4. Phase 2: Admissions and Student Lifecycle

### 4.1 Admissions Module

New package:

```text
com.campus360.admissions
```

Migration:

```text
V8__admissions_lifecycle.sql
```

Tables:

```text
admission_leads
admission_applications
admission_application_documents
admission_status_history
admission_notes
admission_offers
```

Key statuses:

```text
LEAD
APPLICATION_RECEIVED
DOCUMENT_PENDING
UNDER_REVIEW
SHORTLISTED
INTERVIEW_SCHEDULED
APPROVED
REJECTED
WAITLISTED
OFFERED
ENROLLED
CANCELLED
```

Entities:

```text
admissions.domain.AdmissionLead
admissions.domain.AdmissionApplication
admissions.domain.AdmissionDocument
admissions.domain.AdmissionStatusHistory
admissions.domain.AdmissionNote
admissions.domain.AdmissionOffer
```

Repositories:

```text
AdmissionLeadRepository
AdmissionApplicationRepository
AdmissionDocumentRepository
AdmissionStatusHistoryRepository
AdmissionOfferRepository
```

Services:

```text
AdmissionLeadService
AdmissionApplicationService
AdmissionWorkflowService
AdmissionConversionService
```

Controllers:

```text
AdmissionLeadController
AdmissionApplicationController
AdmissionOfferController
```

APIs:

```text
GET  /api/v1/admissions/leads
POST /api/v1/admissions/leads
GET  /api/v1/admissions/applications
POST /api/v1/admissions/applications
GET  /api/v1/admissions/applications/{id}
PUT  /api/v1/admissions/applications/{id}
POST /api/v1/admissions/applications/{id}/notes
POST /api/v1/admissions/applications/{id}/actions/submit-review
POST /api/v1/admissions/applications/{id}/actions/shortlist
POST /api/v1/admissions/applications/{id}/actions/approve
POST /api/v1/admissions/applications/{id}/actions/reject
POST /api/v1/admissions/applications/{id}/actions/waitlist
POST /api/v1/admissions/applications/{id}/actions/create-offer
POST /api/v1/admissions/offers/{id}/actions/accept
POST /api/v1/admissions/applications/{id}/actions/enroll
```

Enrollment conversion must:

- Create or link user account.
- Create student profile.
- Assign student number using `NumberingService`.
- Attach program, section, and term if selected.
- Copy documents to student profile.
- Write audit log.
- Publish notification event.
- Send student welcome email with tenant code, login URL, student number, program, section, and next steps.
- Optionally send parent welcome email when guardian login is enabled.

Done when:

- Admission application can become an enrolled student.
- Status history is visible through API.
- Duplicate email and phone rules are handled tenant-wise.
- Enrolled students receive onboarding information automatically.

### 4.2 Student 360 Upgrade

Migration:

```text
V9__student_360_and_parent_portal.sql
```

Tables:

```text
student_guardians
student_addresses
student_documents
student_lifecycle_history
student_notes
student_tags
student_tag_links
```

Extend `StudentProfile` with:

- admission number
- enrollment date
- lifecycle status
- category/quota
- blood group
- date of birth
- gender
- nationality
- emergency contact
- current academic standing

Statuses:

```text
ACTIVE
TRANSFERRED
SUSPENDED
GRADUATED
ARCHIVED
WITHDRAWN
```

APIs:

```text
GET  /api/v1/students/{id}/profile-360
PUT  /api/v1/students/{id}/personal
PUT  /api/v1/students/{id}/academic
GET  /api/v1/students/{id}/guardians
POST /api/v1/students/{id}/guardians
PUT  /api/v1/students/{id}/guardians/{guardianId}
GET  /api/v1/students/{id}/documents
POST /api/v1/students/{id}/documents
POST /api/v1/students/{id}/actions/promote
POST /api/v1/students/{id}/actions/transfer-section
POST /api/v1/students/{id}/actions/suspend
POST /api/v1/students/{id}/actions/graduate
POST /api/v1/students/{id}/actions/archive
POST /api/v1/students/bulk-import/template
POST /api/v1/students/bulk-import
GET  /api/v1/students/bulk-import/{jobId}
POST /api/v1/students/bulk-import/{jobId}/actions/commit
```

Bulk student import columns:

```text
first_name
last_name
email
phone
admission_number
roll_number
program_code
department_code
section_code
term_code
date_of_birth
gender
guardian_name
guardian_email
guardian_phone
address_line_1
city
state
pincode
category
quota
send_welcome_email
create_parent_account
```

Bulk student import validations:

- Required student name.
- Valid email and phone where provided.
- Program, department, section, and term must exist in same tenant.
- Roll number must be unique within tenant.
- Admission number must be unique within tenant when provided.
- Guardian email must not conflict with unrelated active user unless linking is intended.
- Row duplicates must be detected before commit.
- Welcome emails must be sent only after successful student creation.

Done when:

- Student detail page API gives complete student 360 data.
- Student lifecycle actions are audited and status-tracked.
- Parent/guardian data exists for parent portal.
- Institution admin can import hundreds or thousands of students with validation preview.

### 4.3 Parent Access

Use existing IAM user model with parent role.

Tables:

```text
parent_student_links
```

APIs:

```text
GET  /api/v1/parents/me/students
GET  /api/v1/parents/me/students/{studentId}/overview
GET  /api/v1/parents/me/students/{studentId}/attendance
GET  /api/v1/parents/me/students/{studentId}/results
GET  /api/v1/parents/me/students/{studentId}/fees
```

Rules:

- Parent can only view linked students.
- Parent cannot access unrelated student by id.
- Parent access must be tenant-scoped.

Done when:

- Parent can log in and see only linked child data.

## 5. Phase 3: Academic Operations

### 5.0 Academic Structure Bulk Import

Existing academic setup should support manual entry and bulk import. Institutions usually have many departments, programs, courses, terms, and sections. Manual-only setup will not feel enterprise-ready.

Use the shared import framework from section 3.5.

Import types:

```text
DEPARTMENTS
PROGRAMS
COURSES
TERMS
SECTIONS
CURRICULUM
```

APIs:

```text
GET  /api/v1/academics/import-templates
GET  /api/v1/academics/import-templates/{type}/download
POST /api/v1/academics/imports
GET  /api/v1/academics/imports/{jobId}
POST /api/v1/academics/imports/{jobId}/actions/validate
POST /api/v1/academics/imports/{jobId}/actions/commit
```

Course import columns:

```text
course_code
course_name
department_code
credits
course_type
level
description
active
```

Program import columns:

```text
program_code
program_name
department_code
degree_type
duration_years
active
```

Section import columns:

```text
section_code
program_code
term_code
capacity
class_teacher_email
active
```

Curriculum import columns:

```text
program_code
term_code
course_code
course_category
is_mandatory
credits
display_order
```

Validation rules:

- Codes must be unique within tenant.
- Department code must exist before program/course import.
- Program and term must exist before section/curriculum import.
- Credits must be numeric and non-negative.
- Class teacher email must belong to a faculty user in same tenant.
- Inactive records should not be used for new timetable or enrollment.

Done when:

- Institution admin can complete initial academic setup through bulk import.
- Course import supports validation preview and row-level errors.
- Timetable, attendance, exams, and enrollment can reuse imported structure.

### 5.1 Faculty Module

New package:

```text
com.campus360.faculty
```

Migration:

```text
V10__faculty_and_timetable.sql
```

Tables:

```text
faculty_profiles
faculty_department_assignments
faculty_course_assignments
faculty_workload_snapshots
```

APIs:

```text
GET  /api/v1/faculty
POST /api/v1/faculty
GET  /api/v1/faculty/{id}
PUT  /api/v1/faculty/{id}
POST /api/v1/faculty/{id}/course-assignments
GET  /api/v1/faculty/me/timetable
GET  /api/v1/faculty/me/courses
POST /api/v1/faculty/bulk-import/template
POST /api/v1/faculty/bulk-import
GET  /api/v1/faculty/bulk-import/{jobId}
POST /api/v1/faculty/bulk-import/{jobId}/actions/commit
```

Bulk faculty import columns:

```text
employee_code
first_name
last_name
email
phone
department_code
designation
joining_date
qualification
employment_type
send_welcome_email
```

Faculty onboarding email must include:

- Institution name.
- Tenant code.
- Login URL.
- Employee code.
- Assigned department.
- Assigned role.
- Initial setup/password instructions.

Done when:

- Faculty exists as a first-class profile linked to IAM user.
- Faculty can be assigned courses and sections.
- Faculty can be imported in bulk and receive onboarding email after successful account creation.

### 5.2 Academic Calendar

Package:

```text
com.campus360.academics.calendar
```

Tables:

```text
academic_calendar_events
academic_holidays
```

APIs:

```text
GET  /api/v1/academics/calendar
POST /api/v1/academics/calendar/events
PUT  /api/v1/academics/calendar/events/{id}
DELETE /api/v1/academics/calendar/events/{id}
```

Event types:

```text
HOLIDAY
EXAM
FEE_DUE
PLACEMENT_DRIVE
ACADEMIC_EVENT
ADMISSION_EVENT
CUSTOM
```

Done when:

- Institution can maintain an academic calendar.
- Other modules can publish calendar events.

### 5.3 Timetable Engine

Package:

```text
com.campus360.timetable
```

Tables:

```text
rooms
time_slots
timetable_templates
timetable_entries
timetable_conflicts
```

Entities:

```text
Room
TimeSlot
TimetableTemplate
TimetableEntry
TimetableConflict
```

Services:

```text
RoomService
TimeSlotService
TimetableService
TimetableConflictService
```

APIs:

```text
GET  /api/v1/timetable/rooms
POST /api/v1/timetable/rooms
GET  /api/v1/timetable/time-slots
POST /api/v1/timetable/time-slots
GET  /api/v1/timetable/sections/{sectionId}
PUT  /api/v1/timetable/sections/{sectionId}
GET  /api/v1/timetable/faculty/{facultyId}
GET  /api/v1/timetable/me
POST /api/v1/timetable/actions/validate
POST /api/v1/timetable/actions/publish
```

Conflict checks:

- Same faculty in two places at same time.
- Same room used at same time.
- Same section has two classes at same time.
- Course assigned to wrong program or section.

Done when:

- Timetable can be created and validated.
- Faculty and students can read their own timetable.

### 5.4 Exams and Results

Package:

```text
com.campus360.exams
```

Migration:

```text
V11__exams_results_and_grade_cards.sql
```

Tables:

```text
exam_cycles
exam_components
exam_schedules
exam_mark_sheets
exam_marks
result_publications
grade_cards
result_status_history
```

Statuses:

```text
DRAFT
MARK_ENTRY_OPEN
SUBMITTED
APPROVED
PUBLISHED
LOCKED
REOPENED
```

APIs:

```text
GET  /api/v1/exams/cycles
POST /api/v1/exams/cycles
GET  /api/v1/exams/schedules
POST /api/v1/exams/schedules
GET  /api/v1/exams/mark-sheets
POST /api/v1/exams/mark-sheets
PUT  /api/v1/exams/mark-sheets/{id}/marks
POST /api/v1/exams/mark-sheets/{id}/actions/submit
POST /api/v1/exams/mark-sheets/{id}/actions/approve
POST /api/v1/exams/results/{id}/actions/publish
GET  /api/v1/exams/students/{studentId}/grade-card
GET  /api/v1/exams/reports/results
```

Rules:

- Faculty can enter marks only for assigned courses.
- HOD/admin can approve.
- Published results are read-only unless reopened by authorized role.
- Grade card generation must be repeatable.

Done when:

- Marks can move from draft to published result.
- Student and parent can read published results only.

## 6. Phase 4: Finance Enterprise Depth

Current finance exists. Deepen it instead of rewriting it.

Migration:

```text
V12__finance_enterprise_depth.sql
```

New or extended tables:

```text
fee_categories
fee_components
student_fee_assignments
invoice_line_items
receipts
payment_allocations
fee_concessions
fee_waivers
refunds
finance_status_history
reconciliation_batches
reconciliation_items
```

Services:

```text
FeePlanService
StudentFeeAssignmentService
InvoiceService
ReceiptService
PaymentService
ConcessionService
RefundService
ReconciliationService
FinanceReportService
```

APIs:

```text
GET  /api/v1/finance/fee-categories
POST /api/v1/finance/fee-categories
GET  /api/v1/finance/fee-components
POST /api/v1/finance/fee-components
POST /api/v1/finance/students/{studentId}/fee-assignments
GET  /api/v1/finance/students/{studentId}/ledger
POST /api/v1/finance/invoices
POST /api/v1/finance/invoices/bulk
GET  /api/v1/finance/invoices/{id}
POST /api/v1/finance/invoices/{id}/actions/issue
POST /api/v1/finance/invoices/{id}/actions/waive
POST /api/v1/finance/payments
POST /api/v1/finance/payments/{id}/actions/cancel
GET  /api/v1/finance/receipts/{id}
POST /api/v1/finance/refunds
GET  /api/v1/finance/reports/collection-summary
GET  /api/v1/finance/reports/due-aging
```

Rules:

- Payments must allocate to invoice line items.
- Receipts must use database-backed numbering.
- Cancelling payment must reverse allocations.
- Waivers and concessions must be approved and audited.
- Invoice totals must be calculated server-side.

Done when:

- Finance supports real fee dues, payments, receipts, waivers, refunds, and reports.

## 7. Phase 5: Placement CRM Depth

Current placement exists. Extend it into a CRM and pipeline.

Migration:

```text
V13__placement_crm_depth.sql
```

New or extended tables:

```text
company_contacts
company_notes
company_follow_ups
placement_drives
placement_drive_rounds
application_round_status
interviews
interview_feedback
resume_versions
student_projects
student_certifications
student_internships
offer_compensation_components
placement_status_history
alumni_outcomes
```

Services:

```text
CompanyCrmService
PlacementDriveService
ApplicationPipelineService
InterviewService
ResumeService
OfferCompensationService
PlacementReportService
```

APIs:

```text
GET  /api/v1/placements/companies/{id}/contacts
POST /api/v1/placements/companies/{id}/contacts
POST /api/v1/placements/companies/{id}/notes
POST /api/v1/placements/companies/{id}/follow-ups
GET  /api/v1/placements/drives
POST /api/v1/placements/drives
POST /api/v1/placements/drives/{id}/rounds
POST /api/v1/placements/applications/{id}/actions/move-stage
POST /api/v1/placements/applications/{id}/interviews
POST /api/v1/placements/interviews/{id}/feedback
GET  /api/v1/placements/students/{studentId}/resume-versions
POST /api/v1/placements/students/{studentId}/resume-versions
GET  /api/v1/placements/reports/pipeline
GET  /api/v1/placements/reports/offers
GET  /api/v1/placements/reports/recruiters
```

Pipeline stages:

```text
APPLIED
ELIGIBLE
SHORTLISTED
TEST
TECHNICAL_INTERVIEW
HR_INTERVIEW
SELECTED
REJECTED
OFFER_RELEASED
OFFER_ACCEPTED
JOINED
WITHDRAWN
```

Rules:

- Stage transitions must be validated.
- Every stage change creates history.
- Offer accepted may lock student from additional offers based on tenant policy.
- Recruiter contacts and follow-ups should be tenant-scoped.

Done when:

- Placement team can manage company relationship, drive, rounds, interviews, offers, and reports.

## 8. Phase 6: Communication, Documents, and Service Desk

### 8.1 Document Storage

Migration:

```text
V7__document_storage.sql
```

Package:

```text
com.campus360.document
```

Tables:

```text
documents
document_versions
document_access_logs
document_tags
```

Services:

```text
DocumentStorageService
DocumentAccessService
DocumentMetadataService
```

Storage strategy:

- Start with local filesystem storage for dev.
- Store metadata in database.
- Use a storage interface so S3/Azure Blob can be added later.

Interface:

```java
public interface ObjectStorage {
    StoredObject put(String key, InputStream content, String contentType, long size);
    Resource get(String key);
    void delete(String key);
}
```

APIs:

```text
POST /api/v1/documents
GET  /api/v1/documents/{id}
GET  /api/v1/documents/{id}/download
POST /api/v1/documents/{id}/versions
DELETE /api/v1/documents/{id}
GET  /api/v1/documents?ownerType=STUDENT&ownerId=1
```

Rules:

- All document access must check tenant and owner permission.
- Every download should write `document_access_logs`.

Done when:

- Admissions, students, placements, and service desk can reuse the document module.

### 8.2 Communication Module

Migration:

```text
V14__communication_and_templates.sql
```

Package:

```text
com.campus360.communication
```

Tables:

```text
announcement_channels
announcements
announcement_audiences
announcement_reads
notification_templates
user_notification_preferences
message_delivery_logs
mail_outbox
mail_template_variables
```

APIs:

```text
GET  /api/v1/communication/announcements
POST /api/v1/communication/announcements
GET  /api/v1/communication/announcements/{id}
POST /api/v1/communication/announcements/{id}/actions/publish
GET  /api/v1/communication/templates
POST /api/v1/communication/templates
PUT  /api/v1/communication/templates/{id}
GET  /api/v1/communication/preferences/me
PUT  /api/v1/communication/preferences/me
GET  /api/v1/communication/mail-outbox
POST /api/v1/communication/mail-outbox/{id}/actions/retry
```

Audience types:

```text
INSTITUTION
DEPARTMENT
PROGRAM
SECTION
ROLE
USER
STUDENT
PARENT
FACULTY
```

Done when:

- Admin can publish targeted announcements.
- Users can read relevant announcements.
- Notification templates are reusable by other modules.
- User onboarding, password reset, admission enrollment, fee invoice, receipt, attendance warning, exam result, placement offer, and service request emails are template-driven.

Required mail templates for enterprise V1:

```text
TENANT_APPROVED_ADMIN_WELCOME
USER_INVITATION
STUDENT_ENROLLED_WELCOME
PARENT_ACCOUNT_WELCOME
FACULTY_ACCOUNT_WELCOME
PLACEMENT_OFFICER_WELCOME
PASSWORD_SETUP
PASSWORD_RESET
FEE_INVOICE_ISSUED
PAYMENT_RECEIPT
LOW_ATTENDANCE_ALERT
RESULT_PUBLISHED
PLACEMENT_APPLICATION_STATUS
PLACEMENT_OFFER_RELEASED
SERVICE_REQUEST_CREATED
SERVICE_REQUEST_STATUS_CHANGED
```

Mail outbox rules:

- Insert mail into outbox in the same transaction as the business event.
- Send mail asynchronously after transaction commit.
- Retry failed sends with capped attempts.
- Store provider response, failure reason, attempt count, and last attempted time.
- Never block student enrollment or user creation only because mail delivery failed.

### 8.3 Workflow and Service Desk

Migration:

```text
V15__workflow_service_desk.sql
```

Packages:

```text
com.campus360.workflow
com.campus360.servicedesk
```

Tables:

```text
workflow_definitions
workflow_steps
workflow_instances
workflow_tasks
workflow_history
service_requests
service_request_comments
service_request_sla_events
```

Service request types:

```text
DOCUMENT
CERTIFICATE
ID_CARD
GRIEVANCE
LEAVE
GENERAL_SUPPORT
```

APIs:

```text
GET  /api/v1/service-requests
POST /api/v1/service-requests
GET  /api/v1/service-requests/{id}
POST /api/v1/service-requests/{id}/comments
POST /api/v1/service-requests/{id}/actions/assign
POST /api/v1/service-requests/{id}/actions/approve
POST /api/v1/service-requests/{id}/actions/reject
POST /api/v1/service-requests/{id}/actions/close
GET  /api/v1/workflows/definitions
POST /api/v1/workflows/definitions
```

Done when:

- Student requests and grievances use a common workflow/history model.
- SLA status can be reported.

## 9. Phase 7: Analytics and AI Governance

Migration:

```text
V16__analytics_ai_governance.sql
```

Tables:

```text
report_definitions
report_exports
dashboard_widgets
ai_prompt_templates
ai_usage_events
ai_response_audit
student_risk_scores
placement_readiness_snapshots
```

Services:

```text
AnalyticsReportService
ReportExportService
StudentRiskScoringService
AiGovernanceService
AiUsageService
```

APIs:

```text
GET  /api/v1/analytics/reports/enrollment
GET  /api/v1/analytics/reports/attendance-defaulters
GET  /api/v1/analytics/reports/results
GET  /api/v1/analytics/reports/fee-due-aging
GET  /api/v1/analytics/reports/placement-pipeline
POST /api/v1/analytics/reports/{reportKey}/exports
GET  /api/v1/ai/admin/prompt-templates
PUT  /api/v1/ai/admin/prompt-templates/{id}
GET  /api/v1/ai/admin/usage
GET  /api/v1/ai/admin/audit
```

Rules:

- Reports must be tenant-scoped.
- Platform admin can see tenant-level usage, not private student content unless explicitly allowed.
- AI responses must record usage, module, actor, tenant, and model/provider.
- AI features must respect role permissions.

Done when:

- Management reports are exportable.
- AI usage is trackable and governable.

## 10. Phase 8: Production Hardening

Migration:

```text
V17__production_hardening_indexes.sql
```

Implementation:

- Add missing indexes after observing query patterns.
- Add actuator readiness/liveness separation.
- Add rate limiting for auth and public registration APIs.
- Add security headers.
- Add file size/type validation for document uploads.
- Add request logging with correlation id.
- Add database backup documentation.
- Add Dockerfile and deployment environment template.

Files:

```text
Dockerfile
docker-compose.yml
.env.example
docs/DEPLOYMENT.md
docs/GO_LIVE_CHECKLIST.md
```

Tests:

```text
SecurityHeadersTest
RateLimitTest
HealthEndpointTest
MigrationValidationTest
```

Done when:

- Backend can be deployed predictably to staging/production.
- Secrets are environment-driven.
- Go-live checklist is documented.

## 11. Recommended Work Order

Use this exact order to avoid rework.

1. Stabilize config and error response.
2. Add tenant isolation test support.
3. Add permission model and login audit.
4. Add tenant settings and numbering service.
5. Add account invitation and welcome notification service.
6. Add reusable bulk import framework.
7. Replace invoice in-memory numbering.
8. Add document storage module.
9. Add admissions module.
10. Extend student 360 and parent link.
11. Add bulk student import.
12. Add academic structure and course bulk import.
13. Add faculty module and faculty bulk import.
14. Add timetable module.
15. Add exams/results module.
16. Deepen finance.
17. Deepen placement CRM.
18. Add communication module and mail outbox.
19. Add workflow/service desk.
20. Add analytics exports and AI governance.
21. Add production hardening, indexes, and deployment docs.

## 12. Implementation Checklist Template

Use this checklist for every backend feature:

```text
[ ] Migration added
[ ] Entity added
[ ] Repository added with tenant-safe methods
[ ] DTOs added with validation
[ ] Service added with transaction boundaries
[ ] Controller added with role/permission checks
[ ] Audit logging added
[ ] Notification event added where needed
[ ] Mail template/outbox integration added where users must be informed
[ ] Status history added where needed
[ ] Pagination/filtering added for list endpoints
[ ] Bulk import support added where manual-only entry is not realistic
[ ] Import validation preview and row-level error reporting added
[ ] Unit tests added
[ ] Integration tests added
[ ] Tenant isolation tests added
[ ] OpenAPI annotations added
[ ] docs/FRONTEND_API.md updated
[ ] mvn test passes
```

## 13. First Backend Task to Start

Start with Phase 0, Task 1:

```text
Configuration Hardening
```

Why this first:

- The current app has dev defaults and committed values that should not exist in a live product.
- Production profile must fail fast when secrets are missing.
- It reduces risk before more modules are added.

First task files:

```text
src/main/resources/application.yml
src/main/resources/application-dev.yml
src/main/resources/application-prod.yml
src/main/java/com/campus360/platform/config/CorsProperties.java
src/main/java/com/campus360/platform/config/AppSecurityProperties.java
```

Acceptance criteria:

- Local development still starts with simple defaults.
- Production requires `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, and mail credentials only if mail is enabled.
- No real personal email password is present in committed files.
- CORS allowed origins are configurable.
- Server error details are hidden in production.
