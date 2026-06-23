# Campus360 Enterprise Feature Gap Audit

Date: 2026-06-22

Scope: backend + frontend audit against:

- `docs/ENTERPRISE_IMPLEMENTATION_PLAN.md`
- `docs/BACKEND_TECHNICAL_IMPLEMENTATION_PLAN.md`
- `docs/FRONTEND_ENTERPRISE_IMPLEMENTATION_PLAN.md`
- `docs/FRONTEND_UI_UX_ENTERPRISE_STANDARDS.md`

## 1. Executive Summary

Campus360 has moved significantly beyond the first prototype. The backend now contains migrations through `V13`, and major backend modules exist for admissions, student 360, parent access, faculty, timetable, exams, imports, invitations, notifications, and finance depth.

However, the application is not enterprise-complete yet. The main risks are:

- Backend tests do not pass.
- Several backend endpoints are exposed but return placeholders or `null`.
- Bulk import exists structurally but parsing, validation, commit, and real row-level processing are not implemented.
- Frontend builds successfully, but many backend features have no corresponding screens.
- Frontend API clients exist for several modules, but they are not wired into routes.
- Security/config hardening is incomplete because dev config still contains real-looking mail credentials.
- Several enterprise workflows exist as tables/classes but are not complete user journeys.

Current status: strong backend expansion, frontend still behind backend, and production readiness is not yet acceptable.

## 2. Validation Results

| Check | Result | Notes |
| --- | --- | --- |
| `npm run typecheck` | PASS | Frontend TypeScript compiles. |
| `npm run build` | PASS | Frontend production build succeeds. |
| `mvn "-Dmaven.test.skip=true" package` | PASS | Backend main source packages when tests are skipped. |
| `mvn test` | FAIL | Backend test compilation fails with many unresolved imports from test sources. |

Backend `mvn test` failure is a P0 issue. A project cannot be considered enterprise-ready while the test suite cannot compile.

Observed test failure pattern:

- `src/test/java` imports app packages such as `com.campus360.admissions.web.dto`, `com.campus360.institution.domain`, `com.campus360.iam.web`, and `com.campus360.platform.security`.
- Maven main compile completed, but test compile failed with "package does not exist" and "cannot find symbol" errors.
- This must be fixed before trusting feature behavior.

## 3. Backend Coverage Matrix

| Area | Status | Evidence | Gap |
| --- | --- | --- | --- |
| Configuration hardening | PARTIAL | `application-prod.yml` exists; env placeholders exist. | `application-dev.yml` contains real-looking mail credentials. Dev defaults still include weak JWT secret. |
| Error contract | PARTIAL | `ApiError`, `ValidationError`, `GlobalExceptionHandler` exist. | Need verify all controllers return consistent field errors and frontend renders them. |
| Tenant isolation test harness | PARTIAL | `testsupport` package exists. | `mvn test` fails, so harness is not usable yet. |
| Permissions/login audit | PARTIAL | `Permission`, `UserLoginEvent`, account lock entities exist. | Need confirm permission checks are actually enforced beyond broad roles. |
| Tenant plans/settings | PARTIAL | `platform.subscription` and `TenantSetting` exist. | No visible platform subscription/settings controllers found. Frontend has no plan/settings screens. |
| Numbering service | PARTIAL | `NumberingService` exists with database locking. | `ReceiptService` still uses UUID placeholder instead of `NumberingService`. Need verify invoices use numbering consistently. |
| Bulk import framework | INCOMPLETE | Import entities/repositories/services exist. | `ImportParserService` returns `List.of()`, `ImportValidationService` returns `true`, `ImportCommitService` has no commit logic. |
| Account invitation/welcome | PARTIAL | `AccountInvitationController`, `WelcomeNotificationService`, `OutboxMessage` exist. | Invitation create/list endpoints are missing. Welcome emails still use temporary password in body for enrollment. |
| Admissions | PARTIAL | Admissions controllers/services/entities/migration exist. | Frontend routes missing. Need full UI pipeline, document handling, workflow timeline, and tests. |
| Student 360 | PARTIAL | `Student360Controller`, student lifecycle/domain entities exist. | Frontend still mostly shows basic student table; no full Student 360 route/tabs. |
| Parent portal backend | PARTIAL | `ParentPortalController` exists. | No parent frontend route or app shell area. |
| Academic import | PARTIAL | `AcademicImportController` exists. | Depends on incomplete import parser/validator/commit services. No frontend academic import flow. |
| Faculty | PARTIAL | `FacultyController`, service, domain, repositories exist. | Bulk import endpoints return `null`/empty behavior; no frontend faculty portal/screens. |
| Timetable | PARTIAL | `TimetableController`, service, conflict service, domain exist. | No frontend timetable builder or faculty/student timetable screens. Need verify conflict engine depth. |
| Exams/results | PARTIAL | Exam cycle, mark sheet, result controllers/services/entities exist. | Frontend has API client but no exam screens/routes. Need result publishing UX and parent/student views. |
| Finance phase 4 | PARTIAL | Fee categories/components, assignments, invoice, receipt, refund, reconciliation classes exist. | Ledger returns `List.of()`. Reports use placeholder logic. Receipt numbering uses UUID placeholder. Frontend finance screen is still limited. |
| Placement CRM phase 5 | NOT STARTED/PARTIAL OLD | Original placement module exists. | CRM depth is not implemented: contacts, drives, rounds, interviews, resume versions, offer compensation, pipeline history are missing. |
| Document storage | PARTIAL | `platform.storage` exists. | No complete `/api/v1/documents` controller found. No frontend document center. |
| Communication module | MISSING | Notification/SSE exists. | No `communication` package/controllers for announcements, templates, preferences, mail outbox UI. |
| Service desk/workflow | MISSING | Student life exists. | No generic workflow/service desk module with SLA/history. |
| Analytics/AI governance | MISSING/PARTIAL | Basic analytics and AI exist. | No report exports, AI usage governance, prompt templates, AI audit UI. |
| Production hardening | PARTIAL | Actuator exists. | No Docker/deployment/go-live docs verified; rate limiting/security headers/backup process missing. |

## 4. Critical Backend Gaps

### P0: Backend tests fail

This must be fixed first.

Required action:

- Make `mvn test` compile and run.
- Confirm test source package imports match actual main source packages.
- Confirm test classpath/environment is not broken.
- Add a CI-equivalent command that fails fast.

Acceptance:

- `mvn test` exits 0.
- Existing tests cover auth, onboarding, students, admissions, finance, placements, attendance, and tenant isolation.

### P0: Remove committed real credentials

`src/main/resources/application-dev.yml` contains real-looking mail username/password values.

Required action:

- Remove actual credentials.
- Replace with safe local placeholders.
- Move secrets to `.env` or local untracked config.
- Add `.env.example` with placeholders only.

Acceptance:

- No real email/password/API key is present in tracked files.

### P0: Complete or hide placeholder endpoints

The following backend code exposes incomplete behavior:

- `FacultyController.uploadBulkImport()` returns `null`.
- `FacultyController.getBulkImportJob()` returns `null`.
- `ImportParserService.parseCsv()` returns `List.of()`.
- `ImportValidationService.validateRow()` always returns `true`.
- `ImportCommitService.commitRow()` has no commit behavior.
- `StudentFinanceController.getLedger()` returns `List.of()`.
- `FinanceReportService.getCollectionSummary()` uses placeholder query logic.
- `FinanceReportService.getDueAging()` returns zero buckets.
- `ReceiptService` uses UUID placeholder receipt numbers.

Required action:

- Implement real behavior, or remove/hide endpoints from frontend and docs until implemented.

Acceptance:

- No user-facing endpoint returns placeholder data, `null`, or fake success.

## 5. Frontend Coverage Matrix

| Area | Status | Evidence | Gap |
| --- | --- | --- | --- |
| Frontend build | PASS | `npm run typecheck` and `npm run build` pass. | No automated UI/e2e tests yet. |
| Public pages | IMPLEMENTED | Landing, demo, register, login exist. | Needs screenshot QA on mobile/desktop after every major design change. |
| App shell | PARTIAL | Sidebar + mobile drawer exists. | Role areas only platform/institution/student. Missing faculty, parent, HOD, placement officer areas. |
| Enterprise components | PARTIAL | Enterprise data table, filter bar, import wizard, detail drawer, status timeline exist. | Mostly used only on platform registrations/institutions. Import wizard not wired to real routes. |
| Platform admin | PARTIAL | Overview, registrations, institutions exist. | Missing plans, usage, audit log UI, support, system health, mail delivery UI. |
| Institution admin | PARTIAL | Overview, academics, students, placements, fees, student life exist. | Missing admissions, faculty, timetable, exams, reports, communication, documents, settings, service desk. |
| Student portal | PARTIAL | Overview, opportunities, applications, AI, profile, fees, life exist. | Missing timetable, attendance, results, documents center, service desk history, parent-linked views. |
| Parent portal | MISSING | Parent API client exists. | No routes, app shell area, or navigation. |
| Faculty portal | MISSING | Faculty API client exists. | No routes, app shell area, timetable/attendance/marks UI. |
| HOD portal | MISSING | No dedicated area. | Needs department dashboard, approvals, faculty workload, students at risk. |
| Admissions frontend | MISSING | API client exists. | No route/screens for leads, pipeline, application detail, offer/enroll. |
| Bulk student import UI | MISSING | Student API functions exist. | No route/import wizard wired into Students page. |
| Course/academic import UI | MISSING | Import API exists. | No academic import route or wizard integration. |
| Faculty import UI | MISSING | Faculty API exists. | No faculty routes/import UI. |
| Timetable UI | MISSING | Timetable API exists. | No timetable builder, weekly grid, conflict UI. |
| Exams UI | MISSING | Exams API exists. | No exam cycles, mark sheet, result publishing screens. |
| Finance UI | PARTIAL | Institution and student fees routes exist. | Missing fee categories/components UX, receipts, refunds, ledger, reports, reconciliation, mail status. |
| Placement CRM UI | PARTIAL OLD | Existing placement/posting screens exist. | Missing CRM-grade company detail, contacts, drives, rounds, interviews, resume versions, offer compensation. |
| Communication UI | MISSING | Notification bell exists. | No announcement composer, templates, preferences, mail outbox/retry. |
| Documents UI | PARTIAL/MISSING | Student life document requests exist. | No generic document center, upload/download/access history. |
| Reports UI | MISSING | Basic analytics dashboard exists. | No report center, exports, drill-down reports. |

## 6. Frontend Integration Gaps

### P0: Frontend has API clients without user-facing screens

API clients exist for:

- admissions
- faculty
- timetable
- exams
- parent
- imports
- users/invitations

But there are no route pages for most of these workflows.

Required action:

- Add route files and AppShell navigation for each implemented backend module.
- Do not leave API clients unused; either wire them into UI or mark them as backend-only preparation.

### P1: Import API client bypasses shared API base

`frontend/src/lib/api/imports.ts` uses raw `fetch('/api/v1/...')`, while the rest of the app uses `API_BASE` through `apiRequest`.

Risk:

- File upload calls will fail when backend is not hosted on the same origin as frontend.

Required action:

- Add a shared `apiUploadRequest` helper that uses `API_BASE`, auth store token, and consistent error parsing.

### P1: Enterprise components are not broadly adopted

`EnterpriseDataTable`, `FilterBar`, and `DetailDrawer` are used mainly in platform screens. Older institution/student screens still use simpler tables and dialogs.

Required action:

- Migrate students, academics, fees, placement, and student life pages to the enterprise components.

## 7. Feature-by-Feature Gap List

### Platform Admin

Implemented:

- Registration review.
- Institution list.
- Suspend/activate institution.

Missing:

- Tenant detail page.
- Plans/subscriptions UI.
- Tenant usage/health dashboard.
- Platform audit log UI.
- Support/admin tools.
- Mail delivery status.
- System health page.

### Institution Admin

Implemented:

- Basic dashboard.
- Academic structure CRUD.
- Student list/create/update academics.
- Basic placement management.
- Basic fee management.
- Student life grievance/document request handling.

Missing:

- Admissions pipeline.
- Student 360 detail route.
- Bulk student import UI.
- Faculty management route.
- Bulk faculty import UI.
- Timetable builder.
- Exams/results route.
- Reports center.
- Communication center.
- Document center.
- Settings.
- Audit timeline per record.

### Admissions

Backend:

- Leads, applications, workflow, offers, enrollment conversion exist.

Frontend:

- Missing all admissions screens.

Gaps:

- No lead board.
- No application pipeline.
- No application detail/timeline.
- No offer/enroll UI.
- No document checklist UI.
- No mail status after enrollment.

### Student 360

Backend:

- Student 360 APIs and lifecycle actions exist.

Frontend:

- Basic student list only.

Gaps:

- No Student 360 route.
- No tabs for guardians, documents, lifecycle, notes, fees, attendance, results, placements.
- No promote/suspend/graduate/archive/transfer UI.
- No lifecycle timeline.

### Bulk Imports

Backend:

- Import framework exists structurally.
- Academic import controller exists.
- Student/faculty import endpoints exist.

Frontend:

- Import wizard component exists.
- Not wired into student/course/faculty routes.

Gaps:

- Parser is stubbed.
- Validation always passes.
- Commit does not create records.
- Faculty import endpoints return `null`.
- No row-level error screen.
- No commit summary.
- No failed row download.
- No onboarding mail status after import.

### Faculty

Backend:

- Faculty profile and course assignments exist.

Frontend:

- Faculty API client exists.
- No faculty route or portal.

Gaps:

- No faculty list/detail UI.
- No faculty course assignment UI.
- No faculty timetable view.
- No faculty attendance/marks workspace.
- Bulk import incomplete.

### Timetable

Backend:

- Rooms, slots, templates, entries, conflicts exist.

Frontend:

- Timetable API client exists.
- No screens.

Gaps:

- No timetable builder.
- No weekly grid.
- No conflict review UI.
- No publish workflow UI.
- No faculty/student timetable portal.

### Exams and Results

Backend:

- Exam cycles, mark sheets, marks, result publish/generate grade cards exist.

Frontend:

- Exams API client exists.
- No screens.

Gaps:

- No exam cycle setup.
- No schedule setup.
- No mark entry UI.
- No approval/publish workflow UI.
- No student/parent grade card UI.

### Finance

Backend:

- Fee categories/components, assignments, invoices, receipts, refunds, reconciliation classes exist.

Frontend:

- Basic fee screens exist.

Gaps:

- Ledger endpoint returns empty list.
- Report services are placeholders.
- Receipt numbers use UUID placeholder.
- No receipt download/view UI.
- No refund UI.
- No reconciliation UI.
- No due aging report UI.
- No fee component/category management UX in the current route.
- No mail status for invoice/receipt.

### Placement

Backend:

- Original company/posting/application/offer flow exists.

Frontend:

- Basic placement and posting screens exist.

Gaps:

- No CRM contacts.
- No company notes/follow-ups.
- No drives/rounds.
- No interview scheduling/feedback.
- No resume versions.
- No offer compensation breakup.
- No pipeline history.
- No alumni outcomes.

### Communication/Documents/Service Desk/Reports

Implemented:

- SSE notifications and student-life requests.

Missing:

- Announcement module.
- Notification templates.
- Mail outbox/retry UI.
- Generic document storage API controller and UI.
- Generic service desk and workflow engine.
- Report center/export history.

## 8. Documentation Gaps

`docs/FRONTEND_API.md` is outdated compared with the implemented backend. It primarily documents older APIs and does not fully reflect:

- Admissions.
- Student 360.
- Parent portal.
- Faculty.
- Timetable.
- Exams/results.
- Finance depth.
- Import jobs.
- Account invitations.
- Outbox/mail status.

Required action:

- Update `docs/FRONTEND_API.md` after stabilizing endpoint contracts.

## 9. Recommended Fix Order

Do not add more features before fixing foundation gaps.

### Step 1: Make validation trustworthy

1. Fix `mvn test`.
2. Add a documented validation command set:
   - `mvn test`
   - `npm run typecheck`
   - `npm run build`
3. Ensure these pass before every new phase.

### Step 2: Remove production risks

1. Remove real credentials from tracked config.
2. Use safe local placeholders.
3. Add `.env.example`.
4. Confirm production profile fails fast without required secrets.

### Step 3: Finish backend Phase 1/2 infrastructure

1. Complete import parser/validator/commit.
2. Complete account invitation create/list/resend/revoke.
3. Replace temporary password email with password setup link for production.
4. Expose tenant plans/settings endpoints if required by frontend.

### Step 4: Finish backend Phase 4 finance correctly

1. Replace receipt UUID numbering with `NumberingService`.
2. Implement student ledger.
3. Implement finance reports with correct tenant-wide queries.
4. Add tests for invoice, payment, receipt, ledger, due aging, and refunds.

### Step 5: Bring frontend up to backend coverage

Build screens in this order:

1. Admissions pipeline.
2. Student 360.
3. Student bulk import.
4. Academic/course import.
5. Faculty management.
6. Timetable builder.
7. Exams/results.
8. Finance reports/ledger/receipts/refunds.
9. Parent portal.
10. Faculty portal.

### Step 6: Continue later phases

After backend Phase 4 is complete end-to-end:

1. Placement CRM depth.
2. Communication and mail outbox.
3. Document center.
4. Service desk/workflow.
5. Reports and exports.
6. AI governance.
7. Production hardening.

## 10. Acceptance Criteria for "Phase 4 Complete"

Phase 4 should not be considered complete until all of this is true:

- `mvn test` passes.
- `npm run typecheck` passes.
- `npm run build` passes.
- No real credentials are committed.
- No placeholder/stub endpoint is exposed to users.
- Bulk import can parse, validate, show row-level errors, and commit at least students and courses.
- Student enrollment creates account and queues welcome notification safely.
- Student 360 has backend and frontend detail experience.
- Finance supports fee assignment, invoice, payment, receipt, ledger, due aging, and collection summary.
- Faculty/timetable/exams backend endpoints have matching frontend screens or are explicitly marked as backend-only preparation.
- `docs/FRONTEND_API.md` matches actual API contracts.

