import { createFileRoute, Link } from "@tanstack/react-router";
import { useQuery } from "@tanstack/react-query";
import {
  Clock, Building2, CheckCircle2, PauseCircle, ArrowRight,
  GraduationCap, Users, BookOpen, TrendingUp, AlertTriangle,
  CheckCircle, XCircle
} from "lucide-react";
import { listRegistrations, getAllTenantStats } from "@/lib/api/platform";
import { shortDate } from "@/components/common";
import { Button } from "@/components/ui/button";
import { cn } from "@/lib/utils";

export const Route = createFileRoute("/_authenticated/platform/")({
  component: PlatformDashboard,
});

// ── Tiny stat tile ────────────────────────────────────────────────────────────

function Tile({
  label, value, icon: Icon, accent, danger,
}: { label: string; value: number; icon: React.ElementType; accent?: boolean; danger?: boolean }) {
  return (
    <div className={cn(
      "rounded-xl border p-5 flex items-center gap-4 transition-colors",
      accent ? "border-accent/30 bg-accent/5" : danger ? "border-red-500/20 bg-red-500/5" : "border-border bg-card"
    )}>
      <div className={cn(
        "flex size-10 shrink-0 items-center justify-center rounded-lg border",
        accent ? "border-accent/30 bg-accent/10" : danger ? "border-red-500/20 bg-red-500/10" : "border-border bg-muted/50"
      )}>
        <Icon className={cn("size-5", accent ? "text-accent" : danger ? "text-red-400" : "text-muted-foreground")} />
      </div>
      <div>
        <div className={cn("text-2xl font-bold tabular-nums", accent ? "text-accent" : danger ? "text-red-400" : "text-foreground")}>
          {value.toLocaleString()}
        </div>
        <div className="text-xs text-muted-foreground">{label}</div>
      </div>
    </div>
  );
}

// ── Activity event item ───────────────────────────────────────────────────────

function ActivityItem({ label, sub, status }: { label: string; sub: string; status: string }) {
  return (
    <div className="flex items-center gap-3 py-3 border-b border-border/50 last:border-0">
      <div className={cn(
        "flex size-7 shrink-0 items-center justify-center rounded-full",
        status === "APPROVED" ? "bg-emerald-500/10" : status === "REJECTED" ? "bg-red-500/10" : "bg-amber-500/10"
      )}>
        {status === "APPROVED" ? (
          <CheckCircle className="size-4 text-emerald-400" />
        ) : status === "REJECTED" ? (
          <XCircle className="size-4 text-red-400" />
        ) : (
          <Clock className="size-4 text-amber-400" />
        )}
      </div>
      <div className="flex-1 min-w-0">
        <p className="text-sm font-medium truncate">{label}</p>
        <p className="text-xs text-muted-foreground">{sub}</p>
      </div>
      <span className={cn(
        "text-xs font-medium shrink-0",
        status === "APPROVED" ? "text-emerald-400" : status === "REJECTED" ? "text-red-400" : "text-amber-400"
      )}>{status}</span>
    </div>
  );
}

// ── Top tenants by student count ──────────────────────────────────────────────

function TenantHealthRow({ name, code, students, faculty, status, maxStudents }: {
  name: string; code: string; students: number; faculty: number; status: string; maxStudents: number;
}) {
  const pct = maxStudents > 0 ? Math.round((students / maxStudents) * 100) : 0;
  return (
    <div className="grid grid-cols-[1fr,auto] items-center gap-4 py-3 border-b border-border/50 last:border-0">
      <div className="min-w-0">
        <div className="flex items-center gap-2 mb-1">
          <span className="text-sm font-medium truncate">{name}</span>
          <span className={cn(
            "text-xs px-1.5 py-0.5 rounded-full font-medium",
            status === "ACTIVE" ? "bg-emerald-500/10 text-emerald-400" :
            status === "SUSPENDED" ? "bg-amber-500/10 text-amber-400" :
            "bg-red-500/10 text-red-400"
          )}>{status}</span>
        </div>
        <div className="flex items-center gap-3 text-xs text-muted-foreground mb-1.5">
          <span className="font-mono uppercase">{code}</span>
          <span className="flex items-center gap-1"><GraduationCap className="size-3" />{students.toLocaleString()} students</span>
          <span className="flex items-center gap-1"><BookOpen className="size-3" />{faculty.toLocaleString()} faculty</span>
        </div>
        <div className="h-1.5 rounded-full bg-muted overflow-hidden">
          <div
            className="h-full rounded-full bg-accent transition-all duration-500"
            style={{ width: `${pct}%` }}
          />
        </div>
      </div>
      <div className="text-right">
        <div className="text-lg font-bold tabular-nums">{students.toLocaleString()}</div>
        <div className="text-xs text-muted-foreground">students</div>
      </div>
    </div>
  );
}

// ── Dashboard ─────────────────────────────────────────────────────────────────

function PlatformDashboard() {
  const regs = useQuery({ queryKey: ["registrations", "ALL"], queryFn: () => listRegistrations() });
  const statsQuery = useQuery({ queryKey: ["tenant-stats"], queryFn: getAllTenantStats });

  const allRegs = regs.data ?? [];
  const statsAll = statsQuery.data ?? [];

  const pending = allRegs.filter((r) => r.status === "PENDING");
  const recent = [...allRegs]
    .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
    .slice(0, 8);

  const totalTenants = statsAll.length;
  const activeTenants = statsAll.filter((s) => s.status === "ACTIVE").length;
  const suspendedTenants = statsAll.filter((s) => s.status === "SUSPENDED").length;
  const totalStudents = statsAll.reduce((a, s) => a + s.studentCount, 0);
  const totalFaculty = statsAll.reduce((a, s) => a + s.facultyCount, 0);
  const totalUsers = statsAll.reduce((a, s) => a + s.totalUsers, 0);

  const topTenants = [...statsAll]
    .sort((a, b) => b.studentCount - a.studentCount)
    .slice(0, 6);
  const maxStudents = topTenants[0]?.studentCount ?? 1;

  return (
    <div className="space-y-8">
      {/* Header */}
      <div>
        <h1 className="text-2xl font-bold tracking-tight">Platform Overview</h1>
        <p className="mt-1 text-sm text-muted-foreground">
          Real-time view of all tenants, registrations, and platform-wide usage metrics.
        </p>
      </div>

      {/* Primary metrics */}
      <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-3">
        <Tile label="Pending Registration Requests" value={pending.length} icon={Clock} danger={pending.length > 0} />
        <Tile label="Active Tenants" value={activeTenants} icon={CheckCircle2} accent />
        <Tile label="Suspended Tenants" value={suspendedTenants} icon={PauseCircle} />
      </div>

      {/* Platform-wide usage */}
      <div className="rounded-xl border border-border bg-card p-6">
        <div className="flex items-center gap-2 mb-5">
          <TrendingUp className="size-4 text-accent" />
          <h2 className="font-semibold">Platform-Wide Usage</h2>
          <span className="text-xs text-muted-foreground ml-auto">Across {totalTenants} institutions</span>
        </div>
        <div className="grid grid-cols-3 gap-6 text-center">
          <div>
            <div className="text-3xl font-bold tabular-nums text-accent">{totalStudents.toLocaleString()}</div>
            <div className="flex items-center justify-center gap-1 mt-1 text-xs text-muted-foreground">
              <GraduationCap className="size-3" /> Total Students
            </div>
          </div>
          <div>
            <div className="text-3xl font-bold tabular-nums">{totalFaculty.toLocaleString()}</div>
            <div className="flex items-center justify-center gap-1 mt-1 text-xs text-muted-foreground">
              <BookOpen className="size-3" /> Faculty Members
            </div>
          </div>
          <div>
            <div className="text-3xl font-bold tabular-nums">{totalUsers.toLocaleString()}</div>
            <div className="flex items-center justify-center gap-1 mt-1 text-xs text-muted-foreground">
              <Users className="size-3" /> Total Users
            </div>
          </div>
        </div>
      </div>

      {/* Two column: pending review + top tenants */}
      <div className="grid gap-6 xl:grid-cols-2">
        {/* Pending review */}
        <div className="rounded-xl border border-border bg-card p-5">
          <div className="flex items-center justify-between mb-4">
            <div className="flex items-center gap-2">
              <Clock className="size-4 text-amber-400" />
              <h2 className="font-semibold text-sm">Awaiting Review</h2>
              {pending.length > 0 && (
                <span className="flex size-5 items-center justify-center rounded-full bg-amber-500/10 text-xs font-bold text-amber-400">
                  {pending.length}
                </span>
              )}
            </div>
            <Link to="/platform/registrations">
              <Button variant="ghost" size="sm">
                Review all <ArrowRight className="size-3.5" />
              </Button>
            </Link>
          </div>
          {pending.length === 0 ? (
            <div className="flex flex-col items-center py-8 text-center">
              <CheckCircle2 className="size-8 text-emerald-400/40 mb-2" />
              <p className="text-sm text-muted-foreground">All caught up — no pending requests</p>
            </div>
          ) : (
            <div className="divide-y divide-border/50">
              {pending.slice(0, 5).map((r) => (
                <ActivityItem
                  key={r.id}
                  label={r.institutionName}
                  sub={`${r.institutionCode} · ${shortDate(r.createdAt)}`}
                  status={r.status}
                />
              ))}
            </div>
          )}
        </div>

        {/* Tenant health / top tenants */}
        <div className="rounded-xl border border-border bg-card p-5">
          <div className="flex items-center justify-between mb-4">
            <div className="flex items-center gap-2">
              <Building2 className="size-4 text-accent" />
              <h2 className="font-semibold text-sm">Top Tenants by Student Count</h2>
            </div>
            <Link to="/platform/institutions">
              <Button variant="ghost" size="sm">
                Manage all <ArrowRight className="size-3.5" />
              </Button>
            </Link>
          </div>
          {topTenants.length === 0 ? (
            <div className="flex flex-col items-center py-8 text-center">
              <Building2 className="size-8 text-muted-foreground/20 mb-2" />
              <p className="text-sm text-muted-foreground">No tenants provisioned yet</p>
            </div>
          ) : (
            <div>
              {topTenants.map((s) => (
                <TenantHealthRow
                  key={s.institutionId}
                  name={s.name}
                  code={s.code}
                  students={s.studentCount}
                  faculty={s.facultyCount}
                  status={s.status}
                  maxStudents={maxStudents}
                />
              ))}
            </div>
          )}
        </div>
      </div>

      {/* Recent activity feed */}
      <div className="rounded-xl border border-border bg-card p-5">
        <div className="flex items-center justify-between mb-4">
          <div className="flex items-center gap-2">
            <AlertTriangle className="size-4 text-muted-foreground" />
            <h2 className="font-semibold text-sm">Recent Registration Activity</h2>
          </div>
          <Link to="/platform/registrations">
            <Button variant="ghost" size="sm">View all <ArrowRight className="size-3.5" /></Button>
          </Link>
        </div>
        {recent.length === 0 ? (
          <p className="text-sm text-muted-foreground text-center py-6">No registration activity yet</p>
        ) : (
          <div className="grid gap-1 sm:grid-cols-2">
            {recent.map((r) => (
              <ActivityItem
                key={r.id}
                label={r.institutionName}
                sub={`${r.adminEmail} · ${shortDate(r.createdAt)}`}
                status={r.status}
              />
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
