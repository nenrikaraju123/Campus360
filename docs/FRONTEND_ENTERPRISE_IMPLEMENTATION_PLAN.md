# Campus360 Frontend Enterprise Implementation Plan

This document defines the frontend implementation roadmap for turning Campus360 from a functional prototype into a real enterprise campus operations product.

The current frontend stack is:

- React 18
- Vite
- TanStack Router
- TanStack Query
- Zustand
- Tailwind CSS
- lucide-react
- motion
- sonner

Keep this stack. Do not add a heavy UI framework. The priority is to mature the existing app into a polished, role-based, scalable enterprise UI.

## 1. Current Frontend Assessment

The current frontend has a useful foundation:

- Public landing, demo, registration, login, and password change pages.
- Platform admin area.
- Institution admin area.
- Student portal area.
- Shared UI primitives for buttons, inputs, tables, dialogs, badges, fields, and cards.
- API clients and auth refresh handling.
- Notification bell and SSE support.

The main gaps are:

- Many screens are CRUD-heavy and feel like admin forms, not enterprise workflows.
- Large data workflows do not yet support search, filters, pagination, sorting, exports, or bulk actions.
- Bulk import journeys are missing for students, courses, faculty, academic structure, fees, and companies.
- Role-specific portals are incomplete for faculty, parent, HOD, placement officer, and recruiter.
- Dashboards need decision-focused insight, not only summary cards.
- Detail pages need timelines, related records, documents, notes, and audit history.
- Forms need stronger validation, grouping, review steps, and field-level error handling.
- Mobile/tablet layouts need more deliberate design.
- The design system needs enterprise components that can be reused across modules.

## 2. Frontend Product Principles

Campus360 should feel like a daily operating system for institutions.

Follow these principles:

- Every role should land on a page that answers: what needs attention today?
- Every list should help users find, compare, filter, export, and act.
- Every important record should have a complete detail view.
- Every workflow should show status, history, owner, next step, and blockers.
- Every bulk workflow should include upload, validation preview, row errors, commit, and result summary.
- Every sensitive action should require confirmation and show business impact.
- Every screen should handle loading, empty, error, permission-denied, and success states.
- Every page should be usable on desktop and tablet; student/parent views should be mobile-friendly.

## 3. Phase 0: Frontend Stabilization

Target: 1 week.

Goal: make the current frontend stable, consistent, and ready for larger feature work.

Work:

- Audit every route for layout issues, overlapping text, clipped content, and mobile behavior.
- Fix inconsistent copy, labels, spacing, button placement, and table density.
- Standardize page headers, action areas, empty states, loading states, and error states.
- Add route-level permission checks so users do not see irrelevant pages.
- Improve API error parsing to support the backend enterprise error contract.
- Add global error boundary and route fallback UI.
- Add skeleton loading states for dashboard and table-heavy screens.
- Verify landing page, demo page, login, platform, institution, and student areas at desktop and mobile widths.

Deliverable:

- Existing UI becomes clean, consistent, and demo-safe.

## 4. Phase 1: Enterprise Design System Expansion

Target: 1 to 2 weeks.

Goal: create reusable components before adding large enterprise modules.

Add these shared components:

```text
EnterpriseDataTable
FilterBar
BulkActionToolbar
ColumnVisibilityMenu
PaginationControls
DetailDrawer
StatusTimeline
ImportWizard
FileUploadDropzone
FormSection
FormActions
PageSkeleton
MetricStrip
DashboardPanel
CommandSearch
PermissionGate
ConfirmActionDialog
AuditTimeline
DocumentList
MailDeliveryStatus
```

Implementation rules:

- Tables must support controlled search, filters, sorting, pagination, row actions, and bulk actions.
- Drawers should be used for record preview and secondary detail.
- Dialogs should be used only for short focused tasks.
- Full pages should be used for long workflows, imports, and multi-step forms.
- Forms must group fields into clear sections.
- Long workflows should use step indicators.
- Buttons should use lucide icons for common actions.

Deliverable:

- Future features can be implemented faster and consistently.

## 5. Phase 2: App Shell and Navigation Upgrade

Target: 1 week.

Goal: make navigation feel enterprise-grade for every role.

Work:

- Replace the simple sidebar with role-aware navigation groups.
- Add mobile navigation drawer.
- Add command search for quick navigation and record search.
- Add breadcrumbs for deep workflows.
- Add tenant/institution context display.
- Add user menu with profile, theme, password change, and sign out.
- Add notification center page in addition to notification bell.
- Add permission-aware nav visibility.

Role navigation groups:

```text
Platform Admin:
Overview, Tenants, Registrations, Plans, Usage, Audit Logs, Support, System Health

Institution Admin:
Overview, Admissions, Students, Academics, Faculty, Timetable, Exams, Fees, Placements, Communication, Documents, Service Desk, Reports, Settings

Faculty:
Overview, Timetable, Attendance, Marks, Courses, Students, Requests, Announcements

Student:
Overview, Profile, Timetable, Attendance, Results, Fees, Documents, Placements, Requests, AI Career

Parent:
Overview, Attendance, Results, Fees, Announcements, Requests

Placement Officer:
Overview, Companies, Drives, Jobs, Applicants, Interviews, Offers, Reports

HOD:
Overview, Faculty, Students, Attendance, Results, Approvals, Reports
```

Deliverable:

- Each role has a focused workspace instead of generic shared navigation.

## 6. Phase 3: Platform Admin Console

Target: 2 weeks.

Screens:

- Platform overview dashboard.
- Tenant registration review queue.
- Tenant detail page.
- Tenant subscription and plan management.
- Tenant usage and health.
- Platform audit logs.
- Mail delivery and notification status.
- Support/admin tools.

Key UX:

- Tenant list must support status filters, plan filters, search, sorting, pagination, and export.
- Tenant detail must show health, users, usage, plan, recent activity, audit trail, and actions.
- Registration approval must show submitted data, documents, comments, decision history, and generated admin onboarding status.

Deliverable:

- Platform admin can operate the SaaS control plane professionally.

## 7. Phase 4: Institution Admin Workspace

Target: 3 to 4 weeks.

Screens:

- Institution operations dashboard.
- Academic setup.
- Admissions pipeline.
- Student records.
- Faculty and staff.
- Timetable.
- Attendance monitoring.
- Exams and results.
- Fees and collections.
- Placement operations.
- Communication center.
- Document center.
- Service desk.
- Reports.
- Institution settings.

Dashboard must show:

- Pending admissions.
- Attendance risk.
- Fee dues.
- Upcoming exams.
- Placement pipeline.
- Open grievances/requests.
- Notifications needing action.

Deliverable:

- Institution admin has a real command center, not separate disconnected CRUD pages.

## 8. Phase 5: Admissions and Student 360

Target: 3 weeks.

Screens:

- Admission leads.
- Application pipeline.
- Application detail.
- Offer/enrollment workflow.
- Student list.
- Student 360 profile.
- Student bulk import.
- Parent/guardian management.

Student 360 tabs:

```text
Overview
Personal
Guardians
Academics
Attendance
Results
Fees
Documents
Placements
Requests
Timeline
Audit
```

Bulk student import flow:

1. Download template.
2. Upload file.
3. Map columns if needed.
4. Validate rows.
5. Show summary: total, valid, invalid, duplicate, warnings.
6. Show row-level errors in a searchable table.
7. Commit valid rows or all-or-nothing based on selected mode.
8. Show created students, skipped rows, failed rows, and welcome email status.

Deliverable:

- Institution can enroll students one by one or through enterprise-grade bulk import.

## 9. Phase 6: Academic Setup, Timetable, Attendance, Exams

Target: 4 to 5 weeks.

Screens:

- Department/program/course/term/section management.
- Academic structure import center.
- Curriculum mapping.
- Academic calendar.
- Room and time slot setup.
- Timetable builder.
- Faculty course assignment.
- Attendance entry.
- Attendance monitoring dashboard.
- Exam cycles and schedules.
- Mark entry.
- Result approval and publishing.
- Grade card view/download.

Course import flow:

- Same import wizard as student import.
- Validate department code, course code, credits, course type, active status, and duplicates.
- Show dependencies needed before commit.

Timetable UX:

- Weekly grid view.
- Faculty conflict warnings.
- Room conflict warnings.
- Section conflict warnings.
- Publish workflow.

Deliverable:

- Academic operations become usable for real institutions.

## 10. Phase 7: Fees and Finance

Target: 3 weeks.

Screens:

- Fee plans.
- Student fee assignment.
- Invoice list and detail.
- Bulk invoice generation.
- Payment entry.
- Receipt view/download.
- Waiver/concession workflow.
- Refund workflow.
- Due aging report.
- Collection dashboard.
- Reconciliation import.

Key UX:

- Finance tables must be dense, filterable, exportable, and audit-friendly.
- Invoice detail must show line items, payments, waivers, balance, timeline, and documents.
- Payment success should show receipt and mail delivery status.

Deliverable:

- Finance team can manage real collections and dues.

## 11. Phase 8: Placement CRM

Target: 3 to 4 weeks.

Screens:

- Placement dashboard.
- Company CRM.
- Company detail with contacts, notes, follow-ups, jobs, drives, and hiring history.
- Job posting workflow.
- Placement drive builder.
- Applicant pipeline.
- Interview scheduling.
- Interview feedback.
- Offer management.
- Student career profile.
- Resume versions.
- Placement reports.

Key UX:

- Applicant pipeline should use stage-based workflow.
- Company detail should feel like CRM, not only a company table.
- Offer screens must show compensation, joining date, acceptance status, and student lock policy.

Deliverable:

- Placement office can run recruiting operations inside Campus360.

## 12. Phase 9: Role Portals

Target: 4 weeks.

Student portal:

- Dashboard, timetable, attendance, results, fees, receipts, documents, requests, placements, AI career.

Faculty portal:

- Dashboard, timetable, attendance entry, mark entry, assigned courses, student notes, approvals, announcements.

Parent portal:

- Child overview, attendance, results, fee dues, receipts, announcements, requests.

HOD portal:

- Department dashboard, faculty workload, students at risk, results, attendance, approvals.

Placement officer portal:

- Companies, drives, applicants, interviews, offers, placement reports.

Deliverable:

- Every role gets a daily workspace designed for its actual responsibilities.

## 13. Phase 10: Communication, Documents, Service Desk, Reports

Target: 3 weeks.

Screens:

- Announcement composer.
- Audience selector.
- Notification templates.
- Mail outbox and retry status.
- Document center.
- Document upload and access history.
- Service requests.
- Request detail with comments, status history, SLA, and attachments.
- Report center.
- Export history.

Deliverable:

- Campus360 supports communication, follow-up, documents, support, and management reporting.

## 14. Frontend API Modules to Add

Add these client modules as backend APIs become available:

```text
frontend/src/lib/api/imports.ts
frontend/src/lib/api/invitations.ts
frontend/src/lib/api/admissions.ts
frontend/src/lib/api/faculty.ts
frontend/src/lib/api/timetable.ts
frontend/src/lib/api/exams.ts
frontend/src/lib/api/documents.ts
frontend/src/lib/api/communication.ts
frontend/src/lib/api/servicedesk.ts
frontend/src/lib/api/reports.ts
```

Each module must:

- Export typed request and response models.
- Use shared `apiRequest`.
- Preserve backend pagination contracts.
- Convert only UI-friendly formatting in components, not API clients.

## 15. Frontend Testing Plan

Minimum checks:

- `npm run typecheck`
- `npm run build`

Add tests later for:

- Auth redirects and must-change-password flow.
- Role-based navigation visibility.
- Import wizard states.
- Table filters, pagination, sorting, and bulk actions.
- Form validation and server error display.
- Permission denied states.
- Notification bell and mail status display.
- Mobile screenshots for landing, login, dashboards, student portal, import wizard, and detail pages.

## 16. Implementation Order

1. Stabilize existing UI and fix layout issues.
2. Build design system components.
3. Upgrade app shell and role navigation.
4. Build import wizard and enterprise data table.
5. Add platform console improvements.
6. Add institution dashboard and student 360.
7. Add bulk student import.
8. Add academic structure and course import.
9. Add faculty screens and faculty import.
10. Add timetable, attendance, exams, and results.
11. Add finance depth.
12. Add placement CRM.
13. Add role portals.
14. Add communication, documents, service desk, and reports.
15. Polish responsive behavior and run final build checks.

## 17. Definition of Done

A frontend feature is complete only when:

- It supports loading, empty, error, permission-denied, and success states.
- It is responsive on desktop, tablet, and relevant mobile views.
- It uses shared UI components.
- It has field-level validation where forms exist.
- It handles backend validation errors clearly.
- It supports search/filter/pagination where lists can grow.
- It supports export or bulk actions where real users expect them.
- It has clear confirmation for sensitive actions.
- It has useful success feedback and next-step guidance.
- It passes typecheck and build.

