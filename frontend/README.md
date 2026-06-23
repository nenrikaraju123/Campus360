# Campus360 Frontend

React + Vite SPA for the Campus360 multi-tenant placement platform. **Phase 1** — foundation,
public site, and the full auth surface.

## Stack
- **Vite + React 18 + TypeScript**
- **TanStack Router** (file-based, code-split) + **TanStack Query**
- **Tailwind v4** design tokens (`src/styles.css`) — "Architectural Enterprise": Geist + Geist Mono,
  monochrome canvas, terracotta accent (`#c2410c`), full dark mode
- **Motion** (Framer Motion successor) for page/scroll/stagger/toast animations
- **Zustand** auth + theme stores · **Zod** form validation · **Sonner** toasts

## Run

```bash
cd frontend
npm install
npm run dev      # http://localhost:5173
```

Requires the backend running on `http://localhost:8080` (configurable via `.env` → `VITE_API_BASE_URL`).
The backend's CORS already allows `localhost:5173`.

```bash
npm run build      # production build (dist/)
npm run typecheck  # tsc --noEmit
npm run preview    # serve the production build
```

## What's in Phase 1
- **Landing** (`/`) — hero, capabilities, onboarding steps, CTA, footer; scroll-in animations.
- **Register** (`/register`) — institution registration form (Zod-validated) → animated "pending review" card.
- **Login** (`/login`) — dual tabs: **Institution user** (tenant code + email + password) and **Platform admin** (email + password).
- **Forced password change** (`/change-password`) — blocks the app while `mustChangePassword` is true.
- **Role homes** — `/platform` (SUPER_ADMIN), `/institution` (admin/HOD/faculty/placement), `/student` (STUDENT),
  each behind role guards in an app shell (sidebar + translucent top bar), showing identity + upcoming phases.

## Auth model
- Access token in memory, refresh token in `localStorage`.
- `apiRequest` attaches `Authorization`, and on **401** performs a single-flight refresh then retries; on
  failure it clears the session and the router redirects to `/login`.
- On reload, a persisted refresh token is exchanged for a session before first render (`bootstrapAuth`).
- Role-based routing via `homeRouteForRoles`; route guards in `beforeLoad`.

## Architecture
```
src/
  routes/                file-based routes (__root, index, register, login, _authenticated/*)
  components/
    ui/                  primitives (button, input, card, field)
    motion/              PageTransition, FadeUp, Stagger
    layout/              Brand, PublicNav, AppShell, ThemeToggle
    forms/               RegistrationForm, LoginForm, ChangePasswordForm
    dashboard/           Welcome (role-home stub)
  lib/
    api/                 client (fetch + refresh), auth, registrations, types
    auth/                store (zustand), jwt, roles
    theme.ts             dark-mode store
    utils.ts             cn()
```

Subsequent phases (admin console, academic structure, placement workspace, student portal, SSE) drop into
the same structure — see `docs/FRONTEND_API.md` for the full backend contract.
