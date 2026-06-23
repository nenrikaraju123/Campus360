# Campus360 — Frontend Integration Guide

> Everything a frontend developer needs to build the UI for the Campus360 backend.
> All request/response examples below are **real responses captured from the running API**.

---

## 1. Quick facts

| Thing | Value |
|---|---|
| Base URL (local) | `http://localhost:8080` |
| API prefix | `/api/v1` |
| Format | JSON only (`Content-Type: application/json`) |
| Auth | JWT Bearer (`Authorization: Bearer <accessToken>`) |
| Timestamps | ISO-8601 UTC, e.g. `2026-06-20T09:54:05.641Z` |
| Money (e.g. `ctc`) | plain number (rupees), e.g. `1800000` |
| `cgpa` | number on a 0–10 scale, e.g. `8.4` |
| Interactive docs | Swagger UI at `/swagger-ui.html`; OpenAPI JSON at `/v3/api-docs` |
| Health | `GET /actuator/health` |

**CORS** is enabled for these origins by default (override with env `CAMPUS360_CORS_ALLOWED_ORIGINS`):
`http://localhost:3000`, `http://localhost:5173`, `http://localhost:4200`.
Credentials are allowed and the `Authorization` header is exposed.

**Lists are returned as plain JSON arrays** (no pagination envelope yet) — code defensively for growth.

---

## 2. The tenant model (read this first)

Campus360 is **multi-tenant**: one backend serves many institutions ("tenants").

- A **platform admin** (`SUPER_ADMIN`) operates the platform and approves/creates tenants. Platform
  accounts have **no tenant** and log in **without** a tenant code.
- Every other user (institution admin, student, etc.) **belongs to one institution** identified by a
  **tenant code** (e.g. `AIT`). They log in **with** that tenant code.
- **Email is unique per institution, not globally** — the same email can exist in two institutions, so
  the tenant code is required to disambiguate login.

### Onboarding lifecycle

```
 Public site                Platform admin (SUPER_ADMIN)              Institution
 ───────────                ────────────────────────────             ───────────
 POST /registrations  ──►   GET /platform/registrations?status=PENDING
 (status PENDING)           POST /platform/registrations/{id}/approve  ──►  institution ACTIVE
                                  (returns one-time temp password)           + first INSTITUTION_ADMIN
                            POST /platform/registrations/{id}/reject         (mustChangePassword=true)
```

The institution admin then logs in with the tenant code + temp password, is forced to change it, and
from there manages students, placements, etc.

---

## 3. Roles & permissions

Roles returned in the JWT and on login: one or more of

`SUPER_ADMIN`, `INSTITUTION_ADMIN`, `HOD`, `FACULTY`, `STUDENT`, `PLACEMENT_OFFICER`, `RECRUITER`, `PARENT`, `ALUMNI`.

| Capability | Roles allowed |
|---|---|
| Approve/reject/suspend tenants | `SUPER_ADMIN` |
| Create departments/programs/courses | `INSTITUTION_ADMIN`, `HOD` (courses also `FACULTY`) |
| Create terms | `INSTITUTION_ADMIN` |
| Create sections | `INSTITUTION_ADMIN`, `HOD`, `FACULTY` |
| Create/list students | `INSTITUTION_ADMIN`, `HOD` (list also `FACULTY`, `PLACEMENT_OFFICER`) |
| Update student academics | `INSTITUTION_ADMIN`, `HOD`, `FACULTY` |
| Manage companies/postings | `PLACEMENT_OFFICER`, `INSTITUTION_ADMIN` |
| View eligible students | `PLACEMENT_OFFICER`, `INSTITUTION_ADMIN` |
| Apply to a posting | `STUDENT`, `PLACEMENT_OFFICER`, `INSTITUTION_ADMIN` |
| Make an offer | `PLACEMENT_OFFICER`, `INSTITUTION_ADMIN`, `RECRUITER` |
| Respond to an offer | `STUDENT`, `PLACEMENT_OFFICER`, `INSTITUTION_ADMIN` |
| Placement stats | `PLACEMENT_OFFICER`, `INSTITUTION_ADMIN`, `HOD` |
| AI career tools | `STUDENT`, `PLACEMENT_OFFICER`, `INSTITUTION_ADMIN`, `HOD` |

A call with a valid token but insufficient role returns **403**.

---

## 4. Authentication

### 4.1 Tokens

Login returns an **access token** (JWT, ~15 min) and an **opaque refresh token** (~7 days, single-use/rotating).

- Send the access token on every protected call: `Authorization: Bearer <accessToken>`.
- When a call returns **401**, use the refresh token to get a new pair; if refresh fails, send the user to login.
- Refresh tokens rotate: each `/auth/refresh` invalidates the old refresh token and returns a new one. Store the latest.

Access-token claims (decode for convenience; never trust client-side for authz): `sub` (userId), `email`,
`tenantId` (null for platform admin), `roles`, `iat`, `exp`.

### 4.2 `mustChangePassword`

On the **first login of a provisioned admin**, `mustChangePassword` is `true`. The UI must route the user to a
"set new password" screen and call `POST /api/v1/auth/change-password` before allowing normal use.

### 4.3 Auth endpoints

#### `POST /api/v1/auth/login` — public
Institution user:
```json
{ "tenantCode": "AIT", "email": "asha.admin@ait.edu", "password": "EbYcGsZRAgquFk" }
```
Platform admin (omit `tenantCode`):
```json
{ "email": "admin@campus360.local", "password": "ChangeMe!123" }
```
**200** response (real):
```json
{
  "accessToken": "eyJhbGciOiJIUzM4NCJ9...",
  "refreshToken": "ceedca8c-6026-4d7a-...-559fd7c9d107",
  "tokenType": "Bearer",
  "expiresInSeconds": 900,
  "userId": 2,
  "tenantId": 1,
  "roles": ["INSTITUTION_ADMIN"],
  "mustChangePassword": true
}
```
Failures return **400** with a generic "Invalid credentials" (no user/tenant enumeration), or **403** if the
institution is suspended or the account isn't active.

#### `POST /api/v1/auth/refresh` — public
```json
{ "refreshToken": "ceedca8c-..." }
```
Returns the same shape as login (new access + new refresh token).

#### `POST /api/v1/auth/logout` — public
```json
{ "refreshToken": "ceedca8c-..." }
```
**204**. Revokes that refresh token.

#### `POST /api/v1/auth/change-password` — authenticated
```json
{ "currentPassword": "EbYcGsZRAgquFk", "newPassword": "AshaStrong#2026" }
```
**204**. Clears `mustChangePassword` and revokes other refresh tokens (forces re-login elsewhere).

### 4.4 Error format (RFC 7807 Problem Details)

Every error is JSON like:
```json
{
  "type": "about:blank",
  "title": "Conflict",
  "status": 409,
  "detail": "An institution with this code already exists.",
  "timestamp": "2026-06-20T09:53:00Z"
}
```
Validation errors (400) put field messages in `detail`, e.g. `"adminEmail: must be a well-formed email address"`.
Status codes used: **400** validation/business rule, **401** missing/invalid token, **403** wrong role / suspended,
**404** not found, **409** conflict (duplicate), **500** unexpected.

---

## 5. Endpoint reference

> Auth column: 🌐 public · 🔒 any authenticated · 🛡️ role-restricted (see §3).

### 5.1 Tenant registration (public) & platform admin

| Method | Path | Auth | Purpose |
|---|---|---|---|
| POST | `/api/v1/registrations` | 🌐 | Submit onboarding request |
| GET | `/api/v1/platform/registrations?status=PENDING` | 🛡️ SUPER_ADMIN | Review queue |
| GET | `/api/v1/platform/registrations/{id}` | 🛡️ SUPER_ADMIN | One request |
| POST | `/api/v1/platform/registrations/{id}/approve` | 🛡️ SUPER_ADMIN | Provision tenant |
| POST | `/api/v1/platform/registrations/{id}/reject` | 🛡️ SUPER_ADMIN | Reject |
| POST | `/api/v1/platform/institutions` | 🛡️ SUPER_ADMIN | Direct provision |
| GET | `/api/v1/platform/institutions` | 🛡️ SUPER_ADMIN | List all tenants |
| POST | `/api/v1/platform/institutions/{id}/suspend` | 🛡️ SUPER_ADMIN | Suspend tenant |
| POST | `/api/v1/platform/institutions/{id}/activate` | 🛡️ SUPER_ADMIN | Reactivate tenant |

**Submit registration** — request:
```json
{
  "institutionName": "Acme Institute of Technology",
  "institutionCode": "AIT",
  "type": "UNIVERSITY",
  "adminFullName": "Asha Rao",
  "adminEmail": "asha.admin@ait.edu",
  "contactPhone": "+91-9000000000",
  "message": "Please onboard us"
}
```
**202** response:
```json
{ "id": 1, "status": "PENDING", "message": "Your registration is pending review by the platform administrator." }
```

**Approve** — optional body `{ "notes": "Verified" }`. **200** response (the temp password is shown once):
```json
{
  "institutionId": 1,
  "institutionCode": "AIT",
  "adminEmail": "asha.admin@ait.edu",
  "temporaryPassword": "EbYcGsZRAgquFk",
  "mustChangePassword": true
}
```

**Direct provision** (`POST /platform/institutions`) — request (`password` optional; omit to auto-generate):
```json
{ "institutionName": "Beta College", "institutionCode": "BETA",
  "adminFullName": "Dev Admin", "adminEmail": "dev@beta.edu" }
```
Returns the same `ProvisionResult` shape (with `temporaryPassword` when auto-generated).

A registration object (in the review queue) looks like:
```json
{
  "id": 1, "institutionName": "Acme Institute of Technology", "institutionCode": "AIT",
  "type": "UNIVERSITY", "adminFullName": "Asha Rao", "adminEmail": "asha.admin@ait.edu",
  "contactPhone": "+91-9000000000", "message": "Please onboard us",
  "status": "PENDING", "reviewNotes": null, "reviewedBy": null, "reviewedAt": null,
  "institutionId": null, "createdAt": "2026-06-20T09:53:46.674Z", "updatedAt": "2026-06-20T09:53:46.674Z"
}
```
`status` ∈ `PENDING | APPROVED | REJECTED`. Institution `status` ∈ `ACTIVE | SUSPENDED`.

### 5.2 Academic structure (tenant-scoped)

| Method | Path | Auth | Notes |
|---|---|---|---|
| POST | `/api/v1/departments` | 🛡️ ADMIN/HOD | `{name, code, hodUserId?}` |
| GET | `/api/v1/departments` | 🔒 | list |
| GET | `/api/v1/departments/{id}` | 🔒 | one |
| POST | `/api/v1/programs` | 🛡️ ADMIN/HOD | `{departmentId, name, code, level?, durationTerms?, totalCredits?}` |
| GET | `/api/v1/programs` | 🔒 | list |
| POST | `/api/v1/courses` | 🛡️ ADMIN/HOD/FACULTY | `{departmentId, code, title, creditHours?, type?, description?}` |
| GET | `/api/v1/courses` | 🔒 | list |
| POST | `/api/v1/terms` | 🛡️ ADMIN | `{name, startDate?, endDate?, addDropEnd?, status?}` |
| GET | `/api/v1/terms` | 🔒 | list |
| POST | `/api/v1/sections` | 🛡️ ADMIN/HOD/FACULTY | `{courseId, termId, facultyUserId?, capacity?, schedule?}` |
| GET | `/api/v1/sections?termId={id}` | 🔒 | list (filter optional) |

Course `type` ∈ `CORE | ELECTIVE | LAB`. Program `level` e.g. `UNDERGRADUATE | POSTGRADUATE`.
Term `status` ∈ `PLANNED | ACTIVE | CLOSED`. Dates are `YYYY-MM-DD`.

### 5.3 Students

| Method | Path | Auth | Notes |
|---|---|---|---|
| POST | `/api/v1/students` | 🛡️ ADMIN/HOD | creates a STUDENT login + profile |
| GET | `/api/v1/students` | 🛡️ ADMIN/HOD/FACULTY/PLACEMENT_OFFICER | list |
| GET | `/api/v1/students/{id}` | 🔒 | one |
| PATCH | `/api/v1/students/{id}/academics` | 🛡️ ADMIN/HOD/FACULTY | update CGPA/backlogs/term |

**Create student** — request:
```json
{ "fullName": "Ravi Kumar", "email": "ravi@ait.edu", "password": "Ravi#2026",
  "rollNumber": "AIT-CSE-001", "branch": "CSE", "batchYear": 2026, "programId": null, "admissionDate": null }
```
**201** response (note audit fields and the linked `userId`):
```json
{
  "createdAt": "2026-06-20T09:54:05.641Z", "updatedAt": "2026-06-20T09:54:05.641Z",
  "createdBy": "asha.admin@ait.edu", "updatedBy": "asha.admin@ait.edu",
  "id": 1, "tenantId": 1, "userId": 3, "programId": null,
  "rollNumber": "AIT-CSE-001", "branch": "CSE", "batchYear": 2026,
  "admissionDate": null, "currentTerm": 1, "cgpa": 0, "activeBacklogs": 0
}
```
**Update academics** — `PATCH .../academics` body `{ "cgpa": 8.4, "activeBacklogs": 0, "currentTerm": 6 }` (all optional).

### 5.4 Placement & Career

| Method | Path | Auth | Notes |
|---|---|---|---|
| POST | `/api/v1/placements/companies` | 🛡️ PLACEMENT_OFFICER/ADMIN | `{name, sector?, tier?, website?, description?}` |
| GET | `/api/v1/placements/companies` | 🔒 | list |
| POST | `/api/v1/placements/postings` | 🛡️ PLACEMENT_OFFICER/ADMIN | see below |
| GET | `/api/v1/placements/postings?openOnly=true` | 🔒 | list |
| GET | `/api/v1/placements/postings/{id}` | 🔒 | one |
| GET | `/api/v1/placements/postings/{id}/eligible-students` | 🛡️ PLACEMENT_OFFICER/ADMIN | eligibility engine |
| POST | `/api/v1/placements/postings/{id}/applications` | 🛡️ STUDENT/PLACEMENT_OFFICER/ADMIN | `{studentId}` |
| GET | `/api/v1/placements/postings/{id}/applications` | 🛡️ PLACEMENT_OFFICER/ADMIN | list |
| PATCH | `/api/v1/placements/applications/{id}/status` | 🛡️ PLACEMENT_OFFICER/ADMIN | `{status}` |
| POST | `/api/v1/placements/applications/{id}/offer` | 🛡️ PLACEMENT_OFFICER/ADMIN/RECRUITER | `{ctc?, joiningDate?}` |
| POST | `/api/v1/placements/offers/{id}/respond` | 🛡️ STUDENT/PLACEMENT_OFFICER/ADMIN | `{decision: "ACCEPT"|"DECLINE"}` |
| GET | `/api/v1/placements/stats` | 🛡️ PLACEMENT_OFFICER/ADMIN/HOD | analytics |

Company `tier` ∈ `DREAM | TIER1 | TIER2 | MASS`. Posting `type` ∈ `FULL_TIME | INTERNSHIP | PPO`.
Posting `status` ∈ `DRAFT | OPEN | CLOSED`. Application `status` ∈ `APPLIED | SHORTLISTED | REJECTED | OFFERED | WITHDRAWN`.
Offer `status` ∈ `EXTENDED | ACCEPTED | DECLINED`.

**Create posting** — request (send `eligibility` as a structured object):
```json
{
  "companyId": 1, "title": "Software Engineer", "type": "FULL_TIME",
  "ctc": 1800000, "location": "Bengaluru", "description": "Backend role",
  "eligibility": { "minCgpa": 7.0, "branches": ["CSE"], "maxBacklogs": 0, "batchYear": 2026 },
  "closesAt": null
}
```
**201** response — ⚠️ note `eligibility` comes back as a **JSON string**, so `JSON.parse(posting.eligibility)`:
```json
{
  "id": 1, "tenantId": 1, "companyId": 1, "title": "Software Engineer", "type": "FULL_TIME",
  "ctc": 1800000, "location": "Bengaluru", "description": "Backend role",
  "eligibility": "{\"minCgpa\":7.0,\"branches\":[\"CSE\"],\"maxBacklogs\":0,\"batchYear\":2026}",
  "status": "OPEN", "postedBy": 2, "closesAt": null,
  "createdAt": "2026-06-20T09:54:26.155Z", "updatedAt": "2026-06-20T09:54:26.155Z",
  "createdBy": "asha.admin@ait.edu", "updatedBy": "asha.admin@ait.edu"
}
```
All eligibility fields are optional; a missing field means "no constraint on that dimension".

**Eligible students** — `GET .../eligible-students` returns the array of student profiles that pass the
posting's criteria (same shape as §5.3). Apply will be **rejected (400)** with the specific gaps if a student
isn't eligible, e.g. `"Student not eligible: CGPA 6.50 is below the required 7.0"`.

**Apply** — `POST .../applications` body `{ "studentId": 1 }` → **201**:
```json
{ "id": 1, "tenantId": 1, "postingId": 1, "studentId": 1, "status": "APPLIED",
  "appliedAt": "2026-06-20T09:54:26.731Z", "updatedAt": "2026-06-20T09:54:26.731Z" }
```

**Make offer** — `POST .../offer` body `{ "ctc": 1800000, "joiningDate": "2026-07-01" }` → **201** offer (status `EXTENDED`).
This also flips the application to `OFFERED` and pushes a real-time `OFFER_EXTENDED` notification (see §6).

**Stats** — `GET /placements/stats` → **200**:
```json
{ "totalStudents": 1, "placedStudents": 0, "placementRatePct": 0.0,
  "highestCtc": 0, "averageCtc": 0, "openPostings": 1, "totalOffers": 1 }
```
(`placedStudents` counts students with an **ACCEPTED** offer.)

### 5.5 Campus360 Intelligence (AI)

| Method | Path | Auth | Returns |
|---|---|---|---|
| GET | `/api/v1/ai/students/{id}/readiness` | 🛡️ STUDENT/PLACEMENT_OFFICER/ADMIN/HOD | readiness score + coaching |
| POST | `/api/v1/ai/students/{id}/resume-feedback` | 🛡️ STUDENT/PLACEMENT_OFFICER/ADMIN | `{ "feedback": "..." }` (body: `{resumeText}`) |
| GET | `/api/v1/ai/students/{id}/mock-interview?role=Backend Engineer` | 🛡️ STUDENT/PLACEMENT_OFFICER/ADMIN | `{ "questions": "..." }` |
| GET | `/api/v1/ai/students/{studentId}/job-fit/{postingId}` | 🛡️ STUDENT/PLACEMENT_OFFICER/ADMIN | eligibility + AI rationale |

**Readiness** response (real, with AI disabled):
```json
{
  "studentId": 1, "score": 75, "band": "STRONG",
  "factors": ["CGPA 8.40 -> 50/60", "Program not linked -> missing 15"],
  "coaching": "…model-generated plan when AI is enabled…",
  "aiLive": false
}
```
`band` ∈ `STRONG | DEVELOPING | AT_RISK`. **`aiLive`** tells the UI whether `coaching`/`feedback` text is a real
model response (`true`, when the backend has OpenAI enabled) or the deterministic offline placeholder (`false`).
The **numeric score/band/factors are always real** and computed deterministically regardless of `aiLive`.

`job-fit` response:
```json
{ "studentId": 1, "postingId": 1, "eligible": true, "eligibilityGaps": [], "explanation": "…", "aiLive": false }
```

---

## 6. Real-time notifications (SSE)

`GET /api/v1/notifications/stream` is a **Server-Sent Events** stream scoped to the caller's institution.
Events currently emitted:

| `event` name | When | `data` payload |
|---|---|---|
| `connected` | on subscribe | `"subscribed"` |
| `JOB_POSTED` | a new posting opens | `NotificationEvent` (below) |
| `OFFER_EXTENDED` | an offer is created | `NotificationEvent` |

`NotificationEvent` payload:
```json
{ "tenantId": 1, "type": "JOB_POSTED", "title": "New opportunity: Software Engineer",
  "message": "A new posting is open. Check your eligibility and apply.", "at": "2026-06-20T09:54:26Z" }
```

⚠️ The browser's native `EventSource` **cannot send an `Authorization` header**. Use a fetch-based SSE client
that supports headers — recommended: [`@microsoft/fetch-event-source`](https://www.npmjs.com/package/@microsoft/fetch-event-source):

```ts
import { fetchEventSource } from '@microsoft/fetch-event-source';

await fetchEventSource('/api/v1/notifications/stream', {
  headers: { Authorization: `Bearer ${accessToken}` },
  onmessage(ev) {
    if (ev.event === 'JOB_POSTED' || ev.event === 'OFFER_EXTENDED') {
      const n = JSON.parse(ev.data);   // { type, title, message, at }
      showToast(n.title, n.message);
    }
  },
});
```

---

## 7. Suggested frontend architecture

**Apps / areas** (role-gated routing off the JWT `roles`):
- **Public site** — marketing + "Register your institution" form (`POST /registrations`) + login.
- **Platform Admin console** (`SUPER_ADMIN`) — registration review queue, approve/reject, tenant list, suspend/activate.
- **Institution Admin/Staff** (`INSTITUTION_ADMIN`, `HOD`, `FACULTY`) — academic structure, students, placements.
- **Placement** (`PLACEMENT_OFFICER`) — companies, postings (with an eligibility-rule builder), drives, offers, stats dashboard.
- **Student portal** (`STUDENT`) — profile, eligible postings, apply, offers, AI readiness/resume/mock-interview.

**Auth handling**
- Store `accessToken` in memory (or short-lived storage) and `refreshToken` securely.
- Axios/fetch interceptor: attach `Authorization`; on **401**, call `/auth/refresh` once, retry; on failure, logout.
- After login, if `mustChangePassword` → force the change-password screen before anything else.
- Decode the JWT to drive role-based menus (authoritative checks still happen server-side).

**Eligibility-rule builder** (placement UI): produce the `eligibility` object
`{ minCgpa, branches[], maxBacklogs, batchYear }` — each field optional. Remember to `JSON.parse` it when reading a posting back.

---

## 8. Verified end-to-end happy path

This exact sequence was run successfully against the live backend:

1. `POST /auth/login` as platform admin (no tenantCode) → token.
2. `POST /registrations` (public) → `{id:1, status:"PENDING"}`.
3. `GET /platform/registrations?status=PENDING` → the request.
4. `POST /platform/registrations/1/approve` → `temporaryPassword`.
5. `POST /auth/login` with `tenantCode:"AIT"` + temp password → `mustChangePassword:true`.
6. `POST /auth/change-password` → 204.
7. `POST /auth/login` with new password → `mustChangePassword:false`.
8. `POST /students` → student id 1.
9. `PATCH /students/1/academics` `{cgpa:8.4,...}`.
10. `POST /placements/companies` → company 1.
11. `POST /placements/postings` with eligibility → posting 1.
12. `GET /placements/postings/1/eligible-students` → `[student 1]`.
13. `POST /placements/postings/1/applications` `{studentId:1}` → application 1.
14. `POST /placements/applications/1/offer` → offer 1 (EXTENDED).
15. `GET /placements/stats` → totals.
16. `GET /ai/students/1/readiness` → score 75, band STRONG.

---

## 9. Default credentials & environments

- Platform super-admin (dev seed): `admin@campus360.local` / `ChangeMe!123` (change via
  `CAMPUS360_BOOTSTRAP_SUPER_ADMIN_PASSWORD`).
- Configure the frontend base URL per environment; never hardcode tokens.
- Explore everything interactively at **`/swagger-ui.html`** — it lists every endpoint, schema, and lets you
  authorize with a Bearer token.
