# Campus360 Frontend UI/UX Enterprise Standards

This document defines the visual, interaction, accessibility, and usability standards for Campus360 frontend development.

Campus360 should feel like a mature enterprise operations product: professional, calm, dense, trustworthy, fast, and easy to scan.

## 1. Design Direction

Use a restrained enterprise style:

- Clean light and dark themes.
- Strong information hierarchy.
- Compact but readable layouts.
- Clear actions.
- Consistent spacing.
- No decorative clutter.
- No marketing-style dashboards inside the application shell.
- No overlapping text or clipped controls.
- No oversized hero-style typography inside operational pages.

The product should feel suitable for:

- University administrators.
- College management.
- Faculty.
- Finance teams.
- Placement teams.
- Students.
- Parents.
- Platform operators.

## 2. Visual System

Current theme direction:

- Neutral canvas.
- Warm accent color.
- Rounded corners at 8px or less for enterprise surfaces.
- Cards only where a repeated or framed item is genuinely needed.

Rules:

- Do not use cards inside cards.
- Do not make page sections look like floating cards.
- Use full-width sections or simple constrained layouts.
- Keep border radius restrained.
- Avoid single-color monotony by using status colors, neutral surfaces, and subtle contrast.
- Use icons for recognizable actions.
- Use text buttons only when the action is not obvious from an icon.
- Letter spacing should remain normal except small uppercase labels.
- Do not scale font sizes directly with viewport width.

## 3. Layout Standards

Operational pages should follow this structure:

```text
Page header
Primary action area
Optional metric strip
Filter/search row
Main table or workflow area
Detail drawer or side panel
```

Page header must include:

- Clear title.
- One-line business description.
- Primary action, if applicable.

Do not overload the page header with many buttons. Move secondary actions into menus.

Spacing:

- Use tighter spacing for dashboards and tables.
- Use larger spacing only for public pages or onboarding flows.
- Keep page content aligned to a consistent grid.
- Avoid large empty gaps above important headings.

## 4. Navigation Standards

Use role-specific navigation.

Rules:

- Do not show pages the user cannot access.
- Group navigation by work area.
- Keep labels business-friendly.
- Add breadcrumbs for deep pages.
- Add command search for fast access.
- Add mobile navigation drawer.
- Keep sign out, theme, profile, and password change in user menu.

Navigation labels should avoid technical wording.

Good:

```text
Students
Admissions
Fee Management
Placement Drives
Audit Logs
Reports
```

Avoid:

```text
JWT
Entities
Schemas
Endpoints
Payloads
```

## 5. Dashboard Standards

Dashboards must answer:

- What changed?
- What needs attention?
- What is blocked?
- What should the user do next?

Every dashboard should include:

- Key metrics.
- Attention queue.
- Upcoming events.
- Recent activity.
- Shortcuts to common actions.
- Drill-down links.

Avoid dashboards that only show decorative cards with numbers.

Use dashboard panels for:

- Admissions pending review.
- Students at attendance risk.
- Fee dues and overdue aging.
- Exam publishing status.
- Placement pipeline.
- Open service requests.
- Failed mail notifications.

## 6. Data Table Standards

Every enterprise table must support:

- Search.
- Filters.
- Sorting.
- Pagination.
- Row actions.
- Bulk selection when useful.
- Export where business users expect it.
- Empty state.
- Loading state.
- Error state.

Table UX rules:

- Keep headers short.
- Align numbers and dates consistently.
- Use status badges for workflow state.
- Use action menus for secondary actions.
- Keep destructive actions behind confirmation.
- Use sticky filters for long table screens where useful.
- Avoid wrapping critical IDs unpredictably.

Examples of tables requiring enterprise behavior:

- Students.
- Courses.
- Faculty.
- Admissions.
- Invoices.
- Payments.
- Companies.
- Applications.
- Service requests.
- Audit logs.
- Import rows.

## 7. Form Standards

Forms must be organized by business meaning.

Use sections such as:

- Personal details.
- Academic details.
- Contact details.
- Guardian details.
- Account setup.
- Notification options.

Rules:

- Show required fields clearly.
- Validate on submit and display field-level errors.
- Preserve entered values after failed submit.
- Disable submit while pending.
- Show clear success feedback.
- Keep cancel and submit actions in a predictable footer.
- Use review step for high-impact workflows.

Do not put long forms inside small dialogs. Use full pages or drawers.

Dialogs are acceptable for:

- Short create/edit tasks.
- Confirmations.
- Small status updates.

Use full pages for:

- Admissions application.
- Student enrollment.
- Bulk import.
- Timetable builder.
- Exam result publishing.
- Finance payment workflow.

## 8. Bulk Import UX Standards

Bulk import is a critical enterprise workflow.

Every import wizard must include:

1. Template download.
2. Upload.
3. Validation.
4. Error review.
5. Commit.
6. Result summary.

Import summary must show:

- Total rows.
- Valid rows.
- Invalid rows.
- Duplicate rows.
- Warning rows.
- Rows committed.
- Rows failed.
- Notification/mail status where applicable.

Import error table must support:

- Search by row number or value.
- Filter by error type.
- Download error report.
- Clear error messages.

Import commit must show:

- Confirmation before commit.
- Whether commit mode is all-or-nothing or valid-rows-only.
- Post-commit created/skipped/failed counts.
- Welcome email status for students, parents, faculty, and users.

## 9. Detail Page Standards

Important records need detail pages, not only tables.

Detail pages should include:

- Summary header.
- Status badge.
- Primary actions.
- Key facts.
- Tabs or sections.
- Timeline.
- Notes/comments.
- Documents.
- Audit history where relevant.

Records requiring detail pages:

- Tenant.
- Admission application.
- Student.
- Faculty.
- Course.
- Invoice.
- Company.
- Job posting.
- Placement application.
- Offer.
- Service request.
- Import job.

## 10. Status and Workflow Standards

Every workflow should show:

- Current status.
- Next action.
- Who changed it.
- When it changed.
- Comment/reason.
- History.

Use timelines for:

- Admission application.
- Student lifecycle.
- Invoice/payment.
- Placement application.
- Offer.
- Service request.
- Import job.
- Mail delivery retry.

Status badges should be consistent:

- Success: completed, active, approved, paid.
- Warning: pending, overdue, waitlisted, partial.
- Destructive: rejected, failed, suspended, cancelled.
- Neutral: draft, archived, inactive.

## 11. Notification and Mail UX Standards

Users must understand whether business communication was sent.

Show mail delivery status for:

- Tenant approval.
- Account invitation.
- Student enrollment.
- Parent account creation.
- Faculty onboarding.
- Fee invoice.
- Payment receipt.
- Result publishing.
- Placement offer.
- Service request update.

Mail status values:

```text
Pending
Sent
Failed
Skipped
Retrying
```

Admin screens should allow retry where permitted.

Do not expose raw provider errors to normal users. Show understandable messages.

## 12. Responsive Standards

Desktop:

- Use full sidebar navigation.
- Use data tables with filters.
- Use detail drawers where helpful.

Tablet:

- Collapse dense side panels.
- Keep primary workflows usable.
- Avoid horizontal overflow except controlled table scroll.

Mobile:

- Student and parent portal must be mobile-friendly.
- Admin pages must remain usable for review and light actions.
- Use stacked layouts.
- Use mobile navigation drawer.
- Avoid text overlap and clipped buttons.

Required viewport checks:

```text
390px mobile
768px tablet
1280px desktop
1440px desktop
```

## 13. Accessibility Standards

Minimum rules:

- All interactive controls must be keyboard reachable.
- Buttons must have accessible labels.
- Icon-only buttons need visible tooltip or aria-label.
- Dialogs and drawers must trap focus.
- Forms must associate labels with inputs.
- Color cannot be the only signal for status.
- Text contrast must be readable in light and dark themes.
- Loading states must not trap the user.

## 14. Copywriting Standards

Use business language, not technical language.

Good:

```text
Student enrolled
Welcome email sent
Invoice issued
Payment recorded
Result published
Import completed with errors
```

Avoid:

```text
Payload submitted
JWT expired
Entity created
Mutation failed
Request 500
```

Error messages should say:

- What happened.
- What the user can do next.

Example:

```text
Some rows could not be imported. Download the error report, correct the highlighted fields, and upload again.
```

## 15. Motion Standards

Use motion sparingly:

- Page entrance.
- Row appearance.
- Active navigation indicator.
- Tab indicator.
- Lightweight success feedback.

Avoid motion that slows repeated enterprise work.

Respect reduced motion preferences when implemented.

## 16. Public Page Standards

Public pages should feel polished and client-facing.

Landing page must clearly explain:

- What Campus360 does.
- Which institution problems it solves.
- Which teams use it.
- What workflows it supports.
- Why it is enterprise-ready.

Avoid technical terms like JWT, API, schema, or stack names on client-facing pages.

Demo page should be static-share friendly and visually polished.

## 17. Component Quality Checklist

Every reusable component must:

- Accept className only where useful.
- Have predictable spacing.
- Work in light and dark themes.
- Avoid layout shift.
- Support keyboard interaction where interactive.
- Avoid hidden overflow bugs.
- Support long text safely.
- Be documented through usage in at least one real screen.

## 18. Page Quality Checklist

Every page must pass this checklist:

```text
[ ] Clear page title and description
[ ] Primary action is obvious
[ ] Search/filter exists where data can grow
[ ] Loading state exists
[ ] Empty state exists
[ ] Error state exists
[ ] Permission-denied state exists where relevant
[ ] Form validation is clear
[ ] Success feedback is clear
[ ] Destructive actions require confirmation
[ ] Mobile/tablet layout does not overlap
[ ] Dark mode is readable
[ ] Business copy avoids technical terms
[ ] Typecheck passes
[ ] Build passes
```

