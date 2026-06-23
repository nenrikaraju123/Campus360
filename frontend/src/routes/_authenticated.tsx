import { createFileRoute, Outlet, redirect } from "@tanstack/react-router";
import { useAuthStore } from "@/lib/auth/store";

export const Route = createFileRoute("/_authenticated")({
  beforeLoad: ({ location }) => {
    const { accessToken, user } = useAuthStore.getState();
    if (!accessToken || !user) {
      throw redirect({ to: "/login" });
    }
    // Block everything until a provisioned admin sets a real password.
    if (user.mustChangePassword && location.pathname !== "/change-password") {
      throw redirect({ to: "/change-password" });
    }
  },
  component: () => <Outlet />,
});
