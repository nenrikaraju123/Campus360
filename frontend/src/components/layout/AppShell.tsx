import { Link, useNavigate, useRouterState } from "@tanstack/react-router";
import { motion } from "motion/react";
import {
  LayoutDashboard,
  Building2,
  Users,
  Briefcase,
  Sparkles,
  FileStack,
  Layers,
  LogOut,
  BriefcaseBusiness,
  Menu,
  X,
  UserCog,
  type LucideIcon,
} from "lucide-react";
import { useState } from "react";
import { Brand } from "./Brand";
import { ThemeToggle } from "./ThemeToggle";
import { NotificationsBell } from "./NotificationsBell";
import { Button } from "@/components/ui/button";
import { useAuthStore } from "@/lib/auth/store";
import { logout } from "@/lib/api/auth";
import { cn } from "@/lib/utils";

type Area = "platform" | "institution" | "student";

interface NavItem {
  label: string;
  icon: LucideIcon;
  to: string;
  exact?: boolean;
}

const NAV: Record<Area, { title: string; items: NavItem[] }> = {
  platform: {
    title: "Platform Console",
    items: [
      { label: "Overview", icon: LayoutDashboard, to: "/platform", exact: true },
      { label: "Registrations", icon: FileStack, to: "/platform/registrations" },
      { label: "Tenants", icon: Building2, to: "/platform/institutions" },
    ],
  },
  institution: {
    title: "Institution Workspace",
    items: [
      { label: "Overview", icon: LayoutDashboard, to: "/institution", exact: true },
      { label: "Users & Roles", icon: UserCog, to: "/institution/users" },
      { label: "Academic structure", icon: Layers, to: "/institution/academics" },
      { label: "Students", icon: Users, to: "/institution/students" },
      { label: "Placements", icon: Briefcase, to: "/institution/placement" },
      { label: "Fee Management", icon: FileStack, to: "/institution/fees" },
      { label: "Student Life", icon: Sparkles, to: "/institution/life" },
    ],
  },
  student: {
    title: "Student Portal",
    items: [
      { label: "Overview", icon: LayoutDashboard, to: "/student", exact: true },
      { label: "Opportunities", icon: Briefcase, to: "/student/opportunities" },
      { label: "Applications", icon: FileStack, to: "/student/applications" },
      { label: "AI Career", icon: Sparkles, to: "/student/ai" },
      { label: "Career Profile", icon: BriefcaseBusiness, to: "/student/profile" },
      { label: "Fee Management", icon: FileStack, to: "/student/fees" },
      { label: "Student Life", icon: Sparkles, to: "/student/life" },
    ],
  },
};

export function AppShell({ area, children }: { area: Area; children: React.ReactNode }) {
  const user = useAuthStore((s) => s.user);
  const navigate = useNavigate();
  const pathname = useRouterState({ select: (s) => s.location.pathname });
  const config = NAV[area];
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  function isActive(item: NavItem) {
    return item.exact ? pathname === item.to : pathname.startsWith(item.to);
  }

  async function onLogout() {
    await logout();
    navigate({ to: "/login" });
  }

  return (
    <div className="min-h-screen md:grid md:grid-cols-[256px_1fr]">
      <aside className="hidden border-r border-border bg-card/40 md:flex md:flex-col">
        <div className="flex h-16 items-center border-b border-border px-5">
          <Brand />
        </div>
        <div className="px-3 py-5">
          <p className="px-3 pb-2 text-[10px] font-semibold uppercase tracking-wider text-muted-foreground">
            {config.title}
          </p>
          <nav className="space-y-0.5">
            {config.items.map((item) => {
              const active = isActive(item);
              return (
                <Link
                  key={item.to}
                  to={item.to}
                  className={cn(
                    "relative flex items-center gap-3 rounded-md px-3 py-2 text-sm transition-colors",
                    active
                      ? "font-medium text-accent"
                      : "text-muted-foreground hover:bg-muted hover:text-foreground",
                  )}
                >
                  {active && (
                    <motion.span
                      layoutId="nav-active"
                      className="absolute inset-0 rounded-md bg-accent/10"
                      transition={{ type: "spring", stiffness: 400, damping: 32 }}
                    />
                  )}
                  <item.icon className="relative z-10 size-4 shrink-0" />
                  <span className="relative z-10">{item.label}</span>
                </Link>
              );
            })}
          </nav>
        </div>
        <div className="mt-auto border-t border-border p-3">
          <div className="flex items-center gap-3 rounded-md px-2 py-2">
            <div className="grid size-9 shrink-0 place-items-center rounded-full bg-accent/10 font-mono text-xs font-semibold text-accent">
              {(user?.email ?? "?").slice(0, 2).toUpperCase()}
            </div>
            <div className="min-w-0 flex-1">
              <p className="truncate text-sm font-medium">{user?.email ?? "Signed in"}</p>
              <p className="truncate font-mono text-[11px] text-muted-foreground">
                {user?.roles.join(" · ")}
              </p>
            </div>
          </div>
        </div>
      </aside>

      <div className="flex min-h-screen flex-col">
        <header className="sticky top-0 z-30 flex h-16 items-center justify-between border-b border-border/70 bg-background/70 px-5 backdrop-blur-xl md:px-8">
          <div className="flex items-center gap-3">
            <div className="md:hidden">
              <Button variant="ghost" size="icon" onClick={() => setMobileMenuOpen(true)}>
                <Menu className="size-5" />
              </Button>
            </div>
            <h1 className="hidden text-sm font-medium text-muted-foreground md:block">
              {config.title}
            </h1>
          </div>
          <div className="flex items-center gap-1.5">
            <NotificationsBell />
            <ThemeToggle />
            <Button variant="outline" size="sm" onClick={onLogout}>
              <LogOut className="size-4" />
              <span className="hidden sm:inline">Sign out</span>
            </Button>
          </div>
        </header>

        <main className="flex-1 px-5 py-8 md:px-8 md:py-10">
          <div className="mx-auto max-w-7xl">
            {children}
          </div>
        </main>
      </div>

      {/* Mobile Navigation Drawer */}
      {mobileMenuOpen && (
        <div className="fixed inset-0 z-50 flex md:hidden">
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            className="fixed inset-0 bg-black/60 backdrop-blur-sm"
            onClick={() => setMobileMenuOpen(false)}
          />
          <motion.aside
            initial={{ x: "-100%" }}
            animate={{ x: 0 }}
            exit={{ x: "-100%" }}
            transition={{ type: "spring", damping: 25, stiffness: 200 }}
            className="relative flex w-3/4 max-w-sm flex-col border-r border-border bg-card shadow-2xl"
          >
            <div className="flex h-16 items-center justify-between border-b border-border px-5">
              <Brand />
              <Button variant="ghost" size="icon" onClick={() => setMobileMenuOpen(false)}>
                <X className="size-5" />
              </Button>
            </div>
            <div className="flex-1 overflow-y-auto px-3 py-5">
              <p className="px-3 pb-2 text-[10px] font-semibold uppercase tracking-wider text-muted-foreground">
                {config.title}
              </p>
              <nav className="space-y-0.5">
                {config.items.map((item) => {
                  const active = isActive(item);
                  return (
                    <Link
                      key={item.to}
                      to={item.to}
                      onClick={() => setMobileMenuOpen(false)}
                      className={cn(
                        "relative flex items-center gap-3 rounded-md px-3 py-2 text-sm transition-colors",
                        active
                          ? "bg-accent/10 font-medium text-accent"
                          : "text-muted-foreground hover:bg-muted hover:text-foreground"
                      )}
                    >
                      <item.icon className="relative z-10 size-4 shrink-0" />
                      <span className="relative z-10">{item.label}</span>
                    </Link>
                  );
                })}
              </nav>
            </div>
          </motion.aside>
        </div>
      )}
    </div>
  );
}
