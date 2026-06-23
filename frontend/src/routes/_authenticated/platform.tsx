import { createFileRoute, Outlet, redirect } from "@tanstack/react-router";
import { AppShell } from "@/components/layout/AppShell";
import { useAuthStore } from "@/lib/auth/store";
import { homeRouteForRoles } from "@/lib/auth/roles";

export const Route = createFileRoute("/_authenticated/platform")({
  beforeLoad: () => {
    const user = useAuthStore.getState().user;
    if (!user?.roles.includes("SUPER_ADMIN")) {
      throw redirect({ to: homeRouteForRoles(user?.roles) });
    }
  },
  component: () => (
    <AppShell area="platform">
      <Outlet />
    </AppShell>
  ),
});
