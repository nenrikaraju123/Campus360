import { createRootRoute, Outlet, ScrollRestoration } from "@tanstack/react-router";
import { Toaster } from "sonner";
import { useTheme } from "@/lib/theme";

import { FullPageError } from "@/components/ui/FullPageError";
import { FullPageLoader } from "@/components/ui/FullPageLoader";

export const Route = createRootRoute({
  component: RootComponent,
  pendingComponent: () => <FullPageLoader message="Loading Campus360..." />,
  errorComponent: ({ error, reset }) => <FullPageError error={error} reset={reset} />,
});

function RootComponent() {
  const theme = useTheme((s) => s.theme);
  return (
    <>
      <ScrollRestoration />
      <Outlet />
      <Toaster theme={theme} richColors closeButton position="top-right" />
    </>
  );
}
