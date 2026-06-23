# Campus360 Enterprise Implementation Plan

Backend execution details are defined in `docs/BACKEND_TECHNICAL_IMPLEMENTATION_PLAN.md`.
Frontend execution details are defined in `docs/FRONTEND_ENTERPRISE_IMPLEMENTATION_PLAN.md`.
Frontend UI/UX standards are defined in `docs/FRONTEND_UI_UX_ENTERPRISE_STANDARDS.md`.

## 1. Current Assessment

Campus360 is a good technical foundation, but it is not yet a live enterprise product. The current application has the right starting architecture: multi-tenant backend, platform onboarding, institution workflows, students, academics, fees, placements, student life, notifications, analytics, and an AI layer. The gap is product depth, operational maturity, workflow completeness, visual polish, testing, and production readiness.

The goal should not be to make the UI look better only. The goal should be to make Campus360 feel like a real institutional operating system used daily by administrators, faculty, students, placement teams, finance teams, and platform operators.

## 2. Product Vision

Campus360 should help an institution run the full student lifecycle:

- Attract and admit students.
- Manage departments, programs, courses, batches, sections, and terms.
- Track attendance, assessments, marks, results, progression, and academic risk.
- Manage student records, documents, fees, communication, leave, grievances, and services.
- Run placement operations from company pipeline to offers.
- Give students and parents a clear self-service portal.
- Give management dashboards for academic health, fee collection, student engagement, and placement outcomes.
- Give platform admins full control over tenants, plans, usage, support, and compliance.

## 3. Enterprise Release Standard

Before live launch, every important module must meet these standards:

- Clear role-based navigation and permissions.
- Search, filters, pagination, sorting, export, and bulk actions where data grows.
- Bulk import with validation preview for high-volume setup such as students, courses, faculty, fees, and companies.
- Empty states, loading states, error states, confirmation dialogs, and success feedback.
- Audit logs for sensitive operations.
- Automated email notifications for onboarding, enrollment, fee events, attendance alerts, results, placements, and service requests.
- Tenant-safe APIs and tenant isolation tests.
- Form validation on frontend and backend.
- Production-safe configuration with no hardcoded secrets.
- Database migrations only through Flyway.
- API documentation and sample workflows.
- Automated backend, frontend, and end-to-end test coverage for critical paths.
- Deployment scripts, monitoring, logs, backup strategy, and rollback process.

## 4. Priority Roadmap

### Phase 0: Stabilization and Product Audit

Target: 1 to 2 weeks.

This phase makes the existing product trustworthy before adding many new screens.

- Remove all hardcoded or default secrets from committed config and move them to environment variables.
- Create production, staging, and local configuration profiles.
- Review every API for tenant isolation and permission correctness.
- Add tenant isolation tests for students, fees, academics, placements, notifications, and student life.
- Add consistent backend error response format.
- Add frontend global error boundary and route-level fallback UI.
- Add seed/demo data for a complete institution workflow.
- Create a clean enterprise demo path: platform admin, institution admin, faculty, student, placement officer.
- Fix visual inconsistencies: spacing, table density, mobile layouts, forms, dialogs, badges, navigation.
- Add missing tests around login, onboarding, dashboard loading, student creation, fee invoice, attendance, and placement application.

Deliverable: existing application becomes stable, demoable, and internally consistent.

### Phase 1: Enterprise Platform Foundation

Target: 2 to 3 weeks.

- Platform admin command center with tenant health, subscription status, active users, storage, AI usage, support tickets, and recent incidents.
- Tenant plan management: trial, active, suspended, expired, enterprise.
- Tenant settings: logo, academic year, grading rules, fee policies, placement policies, notification channels.
- User management improvements: invite users, deactivate users, reset password, role assignment, account status, login history.
- Role-based account onboarding emails for institution admins, HODs, faculty, students, parents, placement officers, and recruiters.
- Fine-grained permission model beyond only broad roles.
- Audit log viewer with filters by user, module, action, date, and entity.
- Notification templates and delivery preferences.
- Mail outbox with retry tracking so business actions succeed even when email delivery temporarily fails.
- File/document storage foundation with type, owner, access control, and audit trail.
- Import/export framework for CSV or Excel-like workflows, including validation preview, row-level errors, and commit history.
- System-wide search for students, users, companies, courses, invoices, and applications.

Deliverable: Campus360 starts behaving like a SaaS platform, not only a set of CRUD pages.

### Phase 2: Admissions and Student Lifecycle

Target: 3 to 5 weeks.

- Lead/enquiry management for prospective students.
- Admission application form with source, program preference, status, documents, notes, and owner.
- Application review workflow: received, document pending, shortlisted, interview, approved, rejected, waitlisted.
- Admission offer and enrollment conversion.
- Student 360 profile: personal, academic, guardian, contact, documents, fees, attendance, results, placements, grievances.
- Student document checklist: ID proof, certificates, photos, transfer certificate, custom tenant-specific documents.
- Student lifecycle actions: promote, transfer section, change program, suspend, graduate, archive.
- Guardian/parent records and parent portal login.
- Bulk import students with validation preview, duplicate detection, row-level error report, parent linking, account creation, and welcome email.
- Enrollment workflow that sends students institution name, tenant code, login URL, student number, program, section, and next steps.
- Student ID number generation rules per institution.

Deliverable: institutions can manage real students from admission to graduation.

### Phase 3: Academic Operations

Target: 4 to 6 weeks.

- Academic calendar with holidays, events, exam windows, placement drives, and fee due dates.
- Bulk import for departments, programs, courses, terms, sections, and curriculum mapping.
- Timetable engine for programs, sections, courses, faculty, rooms, and time slots.
- Faculty assignment, faculty bulk import, account creation, welcome email, and workload management.
- Daily attendance by class meeting with bulk entry, edit window, and approval rules.
- Attendance dashboard: student-wise, course-wise, section-wise, defaulters, low attendance alerts.
- Assessment and exam planning: components, marks, weightage, grading scale, result publishing.
- Mark entry workflow with draft, submitted, approved, published states.
- Result sheets, transcripts, grade cards, and downloadable reports.
- Course outcomes and performance analytics.
- Lesson plan and syllabus progress tracking.

Deliverable: academics becomes operationally complete, not only structure management.

### Phase 4: Fees and Finance

Target: 3 to 5 weeks.

- Fee plans by program, batch, category, quota, hostel, transport, and optional services.
- Student fee assignment with scholarships, concessions, waivers, fines, and adjustments.
- Invoice lifecycle: draft, issued, partially paid, paid, overdue, cancelled, waived.
- Receipt generation and printable/downloadable receipts.
- Payment modes: cash, bank transfer, card, UPI, cheque, online gateway-ready adapter.
- Refund and adjustment workflow.
- Due reminders and escalation rules.
- Collection dashboard by date, program, category, pending amount, and overdue aging.
- Finance exports for accounting teams.
- Reconciliation screen for payment gateway or bank uploads.

Deliverable: finance team can track real collections and dues confidently.

### Phase 5: Placement CRM and Career Services

Target: 4 to 6 weeks.

- Employer CRM: companies, contacts, sectors, hiring history, relationship owner, notes, follow-ups.
- Placement calendar: drives, deadlines, pre-placement talks, tests, interviews, offers.
- Job posting workflow with approval, eligibility, documents required, rounds, and status.
- Student career profile: skills, resume, projects, certifications, internships, preferences.
- Resume repository with versioning and review comments.
- Application pipeline: applied, eligible, shortlisted, test, interview, selected, rejected, offer released, joined.
- Interview scheduling and feedback forms.
- Offer management with CTC breakup, joining date, acceptance, decline, and placement lock rules.
- Placement analytics: eligible pool, applications, shortlist ratio, offer ratio, department performance, top recruiters.
- Alumni outcomes and recruiter engagement history.

Deliverable: placement office gets a CRM-grade workflow.

### Phase 6: Portals for Daily Users

Target: 3 to 5 weeks.

- Student portal: dashboard, timetable, attendance, marks, fee dues, receipts, documents, placement opportunities, applications, offers, grievances, leave, notifications.
- Faculty portal: timetable, attendance entry, marks entry, student notes, course progress, approvals, announcements.
- Parent portal: attendance, marks, fee dues, receipts, announcements, leave requests, grievances.
- Placement officer portal: employer pipeline, drives, applicants, interviews, offers, analytics.
- HOD portal: department dashboard, faculty workload, students at risk, attendance, results, approvals.
- Institution admin portal: operations command center across academics, finance, students, communication, and placements.

Deliverable: each role has a focused daily workspace instead of sharing generic admin screens.

### Phase 7: Communication, Workflow, and Service Desk

Target: 2 to 4 weeks.

- Unified announcement module for institution, department, program, section, students, parents, and faculty.
- Notification preferences per user and tenant.
- Email/SMS/WhatsApp-ready provider abstraction.
- Template management for admission, fees, attendance, exams, placements, leave, grievances, and approvals.
- Service desk for student requests: documents, certificates, ID cards, grievances, leave, general support.
- Approval workflows with configurable approvers, status history, comments, and attachments.
- SLA tracking for grievances and document requests.

Deliverable: the platform supports real institutional communication and follow-up.

### Phase 8: Analytics and AI

Target: 3 to 5 weeks.

- Management dashboards: enrollment, attendance, academic performance, fee collection, placements, student services.
- Drill-down dashboards from institution to department, program, section, course, and student.
- At-risk student scoring using attendance, marks, fee issues, and engagement.
- Placement readiness score with transparent factors and recommended actions.
- Natural-language assistant for students and administrators, limited to permitted data.
- AI usage limits by tenant and role.
- AI prompt/version management and response audit.
- Exportable reports for management meetings.

Deliverable: Campus360 becomes insight-led, not only transaction-led.

### Phase 9: Production Hardening and Go-Live

Target: 2 to 4 weeks.

- Docker build for backend and frontend.
- Production database migration process.
- CI pipeline: backend tests, frontend typecheck, frontend build, linting, migration check.
- Staging deployment with realistic demo tenant.
- HTTPS, CORS hardening, rate limiting, secure cookies if applicable, security headers.
- Monitoring: health checks, logs, metrics, request correlation, alerts.
- Backup and restore process for database and documents.
- Data retention and deletion policies.
- Admin support tools for tenant troubleshooting.
- Load test key APIs and optimize slow queries.
- Go-live checklist and rollback plan.

Deliverable: application can be safely deployed and operated.

## 5. Screen Map Needed for Enterprise V1

### Public Website

- Landing page.
- Product demo page.
- Institution registration page.
- Contact/sales enquiry page.
- Login page.

### Platform Admin

- Platform dashboard.
- Tenant registration review.
- Tenant list and tenant detail.
- Subscription/plan management.
- Global users and support access.
- Audit logs.
- System health.
- Usage analytics.

### Institution Admin

- Institution dashboard.
- Academic setup.
- Admissions.
- Student records.
- Faculty and staff.
- Timetable.
- Attendance.
- Exams and results.
- Fees.
- Placements.
- Communication.
- Documents.
- Reports.
- Settings.

### Faculty

- My timetable.
- Attendance entry.
- Marks entry.
- Course progress.
- Student list.
- Announcements.
- Approvals.

### Student

- My dashboard.
- Profile.
- Timetable.
- Attendance.
- Marks/results.
- Fees and receipts.
- Documents.
- Leave and grievances.
- Placement opportunities.
- Applications and offers.
- Career readiness and AI support.

### Parent

- Child overview.
- Attendance.
- Results.
- Fees.
- Announcements.
- Requests.

### Placement Officer

- Placement dashboard.
- Companies.
- Contacts.
- Job postings.
- Drives.
- Applicants.
- Interviews.
- Offers.
- Reports.

## 6. Backend Work Packages

- Add missing modules: admissions, faculty, timetable, exams, parent, document management, communication, service desk, subscription, support.
- Convert broad role checks into fine-grained permissions where workflows become complex.
- Add tenant-aware query tests for every repository and service.
- Add validation annotations and domain-level validation for every write request.
- Add consistent pagination for all list APIs.
- Add export endpoints for major reports.
- Add document upload/download APIs with secure ownership checks.
- Add workflow history tables for approvals and status transitions.
- Add stable numbering services for invoices, receipts, admissions, students, and certificates.
- Replace in-memory or simple sequence logic with database-backed counters where uniqueness matters.
- Add optimistic locking to important financial and workflow entities.
- Add domain events for major actions and notification fan-out.

## 7. Frontend Work Packages

- Build a consistent enterprise shell for each role.
- Create reusable components for data tables, filters, action menus, empty states, detail drawers, timeline, file upload, status history, and report cards.
- Add responsive layouts for desktop, tablet, and mobile.
- Add proper form validation and field-level error messages.
- Add confirmation dialogs for destructive or irreversible actions.
- Add bulk action patterns for students, invoices, attendance, and applications.
- Add dashboard drill-down flows instead of static summary cards.
- Add print/download layouts for receipts, grade cards, offer letters, and reports.
- Add route-level access control so users do not see irrelevant pages.
- Add polished demo data presentation for client walkthroughs.

## 8. Data and Reporting Requirements

- Every major entity should include created by, created date, updated by, updated date, status, tenant, and audit history where relevant.
- Every report should support filters, export, and drill-down.
- Every operational dashboard should answer: what needs attention today, what is delayed, what changed recently, and what action should the user take next.
- Reports needed for V1: enrollment, student list, attendance defaulters, exam results, fee due aging, collection summary, placement pipeline, offer summary, grievance SLA, document request status.

## 9. Security and Compliance Requirements

- No committed production credentials.
- Strong password and reset policy.
- Account lockout or throttling for repeated login failure.
- Optional MFA for platform admins and institution admins.
- Tenant data isolation tests.
- Audit all sensitive actions: login failures, user changes, fee changes, payment records, result publishing, tenant changes, data export, document access.
- Role and permission review screen.
- Secure file access with tenant and owner checks.
- Production CORS and security headers.
- Backup, restore, and retention policy.

## 10. Testing Strategy

- Backend unit tests for domain services.
- Backend integration tests for every controller role and tenant boundary.
- Repository tests for tenant-scoped queries.
- Frontend component tests for shared UI behavior.
- End-to-end tests for critical workflows:
  - Platform admin approves institution.
  - Institution admin creates academic structure.
  - Institution admin imports/creates students.
  - Faculty marks attendance.
  - Admin publishes marks.
  - Finance generates invoice and records payment.
  - Placement officer posts job.
  - Student applies and receives offer.
  - Student/parent raises request.
- Visual checks for landing page, demo page, dashboards, and mobile layouts.

## 11. Recommended Enterprise V1 Scope

Do not try to build every possible campus feature before launch. Build a strong V1 that can be sold and extended.

Enterprise V1 should include:

- Platform tenant management.
- Institution setup.
- User and role management.
- Admissions to student conversion.
- Student 360 profile.
- Attendance.
- Exams and results.
- Fees and receipts.
- Placement CRM.
- Student portal.
- Faculty portal.
- Parent portal basic view.
- Communication and notifications.
- Reports and exports.
- Audit logs.
- Production deployment.

Move hostel, transport, library, inventory, payroll, advanced LMS, and advanced accounting to V1.5 or V2 unless a client specifically requires them for launch.

## 12. Execution Order

1. Stabilize security, configuration, test coverage, and UI consistency.
2. Build missing domain foundations: admissions, faculty, timetable, exams, documents, parent access.
3. Deepen existing modules: academics, fees, placements, student life.
4. Upgrade dashboards and reporting.
5. Add workflow automation and communication.
6. Harden for production deployment.
7. Prepare client demo tenant with realistic data and a scripted walkthrough.

## 13. Definition of Done

A feature is not complete until:

- Backend API exists with validation, tenant isolation, permission checks, and tests.
- Frontend screen supports list, create/edit where needed, filters, empty/loading/error states, and responsive layout.
- Sensitive actions are audited.
- Notifications or workflow history are created where users need follow-up.
- Data can be exported where business users expect it.
- The workflow is demoable with realistic seed data.
- Documentation is updated.
