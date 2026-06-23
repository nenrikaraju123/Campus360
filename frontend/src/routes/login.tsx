import { createFileRoute, Link, redirect } from "@tanstack/react-router";
import { motion } from "motion/react";
import { Brand } from "@/components/layout/Brand";
import { ThemeToggle } from "@/components/layout/ThemeToggle";
import { LoginForm } from "@/components/forms/LoginForm";
import { useAuthStore } from "@/lib/auth/store";
import { homeRouteForRoles } from "@/lib/auth/roles";

export const Route = createFileRoute("/login")({
  beforeLoad: () => {
    const { accessToken, user } = useAuthStore.getState();
    if (accessToken && user && !user.mustChangePassword) {
      throw redirect({ to: homeRouteForRoles(user.roles) });
    }
  },
  component: LoginPage,
});

function LoginPage() {
  return (
    <div className="grid min-h-screen lg:grid-cols-2">
      {/* Brand panel */}
      <div className="relative hidden flex-col justify-between overflow-hidden bg-foreground p-12 text-background lg:flex">
        <div className="bg-grid pointer-events-none absolute inset-0 opacity-[0.07]" />
        <div className="relative">
          <Brand className="[&_span:last-child]:text-background" />
        </div>
        <motion.div
          initial={{ opacity: 0, y: 16 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5, ease: [0.16, 1, 0.3, 1] }}
          className="relative max-w-md"
        >
          <p className="font-mono text-xs uppercase tracking-widest text-accent">Campus360</p>
          <h2 className="mt-4 text-3xl font-semibold leading-tight">
            One platform for every campus, every role.
          </h2>
          <p className="mt-4 text-background/70">
            Sign in with your institution's tenant code, or as a platform administrator.
          </p>
        </motion.div>
        <div className="relative font-mono text-xs text-background/40">
          Secured with JWT · rotating refresh tokens
        </div>
      </div>

      {/* Form panel */}
      <div className="relative flex flex-col">
        <div className="flex items-center justify-between p-6 lg:justify-end">
          <div className="lg:hidden">
            <Brand />
          </div>
          <ThemeToggle />
        </div>
        <div className="flex flex-1 items-center justify-center px-6 pb-16">
          <motion.div
            initial={{ opacity: 0, y: 16 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.4, ease: [0.16, 1, 0.3, 1] }}
            className="w-full max-w-sm"
          >
            <h1 className="text-2xl font-semibold tracking-tight">Welcome back</h1>
            <p className="mt-1.5 text-sm text-muted-foreground">
              Sign in to continue to your workspace.
            </p>
            <div className="mt-8">
              <LoginForm />
            </div>
            <p className="mt-8 text-center text-sm text-muted-foreground">
              New institution?{" "}
              <Link to="/register" className="font-medium text-accent hover:underline">
                Register here
              </Link>
            </p>
          </motion.div>
        </div>
      </div>
    </div>
  );
}
