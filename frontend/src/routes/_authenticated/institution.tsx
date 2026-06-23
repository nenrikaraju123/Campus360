import { createFileRoute, Outlet, redirect } from "@tanstack/react-router";
import { AppShell } from "@/components/layout/AppShell";
import { useAuthStore } from "@/lib/auth/store";
import { hasAnyRole, homeRouteForRoles, INSTITUTION_ROLES } from "@/lib/auth/roles";

export const Route = createFileRoute("/_authenticated/institution")({
  beforeLoad: () => {
    const user = useAuthStore.getState().user;
    if (!hasAnyRole(user?.roles, INSTITUTION_ROLES)) {
      throw redirect({ to: homeRouteForRoles(user?.roles) });
    }
  },
  component: () => (
    <AppShell area="institution">
      <Outlet />
    </AppShell>
  ),
});
