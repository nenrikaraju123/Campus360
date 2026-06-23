import { createFileRoute, Outlet, redirect } from "@tanstack/react-router";
import { AppShell } from "@/components/layout/AppShell";
import { useAuthStore } from "@/lib/auth/store";
import { homeRouteForRoles } from "@/lib/auth/roles";

export const Route = createFileRoute("/_authenticated/student")({
  beforeLoad: () => {
    const user = useAuthStore.getState().user;
    if (!user?.roles.includes("STUDENT")) {
      throw redirect({ to: homeRouteForRoles(user?.roles) });
    }
  },
  component: () => (
    <AppShell area="student">
      <Outlet />
    </AppShell>
  ),
});
