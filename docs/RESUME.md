# Campus360 — Work Checkpoint (resume here)

_Last saved mid-build of the frontend Phase 5 (student portal)._

## How to bring the system back up

**Backend** (Spring Boot, Postgres on localhost:5432, db/role `campus360`/`campus360`):
```bash
cd <repo-root>
DB_URL="jdbc:postgresql://localhost:5432/campus360" DB_USERNAME=campus360 DB_PASSWORD=campus360 \
  mvn -q -DskipTests spring-boot:run
# health: http://localhost:8080/actuator/health ; Swagger: /swagger-ui.html
```
(Postgres superuser password is `Root123`; the `campus360` db/role already exist.)

**Frontend** (Vite/React on 5173):
```bash
cd frontend
npm install   # already installed
npm run dev   # http://localhost:5173
```

**Dev logins:** platform admin `admin@campus360.local` / `ChangeMe!123` (no tenant code).
Tenant `AIT`: `asha.admin@ait.edu` / `AshaStrong#2026`.

---

## Status

### Backend — DONE & verified live
- Full `com.campus360` modular monolith: IAM (tenant-scoped JWT auth + rotating refresh + change-password),
  multi-tenancy, onboarding (registration → SUPER_ADMIN approve/reject → provision; suspend/activate),
  institution academics, students, placement (JSON eligibility engine), AI (offline fallback + Spring AI/OpenAI),
  real-time SSE notifications, Flyway V1–V3, CORS for 5173, health UP.
- Added for the student portal: `GET /students/me`, `GET /placements/my/applications`, `GET /placements/my/offers`.
- Contract documented in `docs/FRONTEND_API.md`.

### Frontend — Phases 1–4 DONE, Phase 5 IN PROGRESS
Built and (1–2) build-verified:
- **Phase 1**: landing, `/register`, `/login` (dual tab), `/change-password`, role-home shells, auth plumbing
  (single-flight 401→refresh→retry, bootstrap on reload), theme/dark mode, Motion animations. Design tokens
  in `frontend/src/styles.css` (Geist, terracotta `#c2410c`).
- **Phase 2**: `platform.index` (dashboard), `platform.registrations` (approve/reject + temp-password result),
  `platform.institutions` (list + suspend/activate + create).
- **Phase 3**: `institution.index` (dashboard), `institution.academics` (5 tabs: depts/programs/courses/terms/sections),
  `institution.students` (create + edit academics).
- **Phase 4**: `institution.placement` (postings/companies/stats tabs + eligibility-rule builder),
  `institution.posting.$postingId` (eligible students, applications, shortlist, make-offer).
- **Phase 6 (SSE)**: DONE — `lib/notifications.ts` (fetch-event-source store) + `NotificationsBell` wired into
  `AppShell` (live toasts + bell dropdown for JOB_POSTED / OFFER_EXTENDED).

Infrastructure already in place for Phase 5:
- API modules: `lib/api/{platform,academics,students,placement,ai}.ts`, `entities.ts`.
- Shared UI: `components/ui/{badge,dialog,select,table}.tsx`, `components/common.tsx`
  (PageHeader, StatCard, TabBar, DataState, EmptyState, `moneyINR`, `shortDate`).
- `components/student/ReadinessCard.tsx` (animated score ring + factors + coaching).

---

## ⚠️ FIRST STEP ON RESUME
A full `npm run build` has **not** been run since the Phase 3/4 files and `ReadinessCard` were added.
Run it and fix any errors before continuing:
```bash
cd frontend && npm run build
```
Likely-clean, but check. Common gotcha already fixed once: `StatusBadge` is imported from
`@/components/ui/badge` (NOT from `@/components/common`).

---

## REMAINING WORK — Phase 5 student portal (4 route files)

All under `frontend/src/routes/_authenticated/`. They use `getMyProfile()` (`lib/api/students.ts`) to get the
current student's profile id, then call placement/AI endpoints. Use TanStack Query + the shared components.

1. **`student.index.tsx`** (REPLACE the current stub):
   - `useQuery(['myProfile'], getMyProfile)` → then `useQuery(['readiness', me.id], () => readiness(me.id))`.
   - Render `<ReadinessCard report={...}/>` + identity + quick links to opportunities/applications/ai.

2. **`student.opportunities.tsx`** (NEW):
   - `listPostings(true)` (open only) + `getMyProfile`. Table/cards of postings with eligibility chips
     (`JSON.parse(posting.eligibility)`), `moneyINR(ctc)`.
   - "Apply" → `applyToPosting(postingId, me.id)`; on 400 show the eligibility-gap detail in a toast.
   - Optional "Check fit" → `jobFit(me.id, postingId)` shown in a Dialog (eligible + gaps + explanation).

3. **`student.applications.tsx`** (NEW):
   - `myApplications()` + `myOffers()` + `listPostings(false)` (to map postingId→title) + `getMyProfile`.
   - Two sections: My applications (status badges), My offers (ctc, joiningDate, status) with
     Accept/Decline → `respondToOffer(offerId, 'ACCEPT'|'DECLINE')`, invalidate `['myOffers']`.

4. **`student.ai.tsx`** (NEW):
   - `getMyProfile` → tabs (TabBar): Readiness (`<ReadinessCard/>`), Resume feedback (textarea → `resumeFeedback`),
     Mock interview (role input → `mockInterview`), Job-fit (posting select → `jobFit`).
   - Each shows returned text; note `aiLive` flag (offline vs live).

Route id strings for `createFileRoute`: `/_authenticated/student/`, `/_authenticated/student/opportunities`,
`/_authenticated/student/applications`, `/_authenticated/student/ai`.

The AppShell student nav already links to `/student/opportunities`, `/student/applications`, `/student/ai`.

---

## After Phase 5
- `npm run build` + `npm run typecheck` clean.
- Manually click through all three role areas in the browser (servers must be running).
- Optional polish: pagination on long tables, optimistic updates, enable OpenAI (`AI_ENABLED=true` + `OPENAI_API_KEY`)
  to make AI text live.
