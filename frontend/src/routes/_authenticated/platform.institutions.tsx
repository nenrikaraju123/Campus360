import { useState, useMemo } from "react";
import { createFileRoute } from "@tanstack/react-router";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import {
  Plus, Power, PowerOff, Copy, Loader2, Building2, Pencil, Trash2,
  Users, GraduationCap, BookOpen, ChevronRight, AlertTriangle, CheckCircle,
  XCircle, Search, MapPin, Calendar, BarChart3, RefreshCw
} from "lucide-react";
import { toast } from "sonner";
import {
  listInstitutions, createInstitution, suspendInstitution, activateInstitution,
  updateInstitution, deactivateInstitution, getAllTenantStats,
} from "@/lib/api/platform";
import type { Institution, TenantStats, ProvisionResult } from "@/lib/api/entities";
import { ApiError } from "@/lib/api/client";
import { shortDate } from "@/components/common";
import { Dialog } from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Select } from "@/components/ui/select";
import { Field } from "@/components/ui/field";
import { StatusBadge } from "@/components/ui/badge";
import { cn } from "@/lib/utils";

export const Route = createFileRoute("/_authenticated/platform/institutions")({
  component: InstitutionsPage,
});

// ── Status helpers ────────────────────────────────────────────────────────────

function statusColor(status: string) {
  if (status === "ACTIVE") return "text-emerald-400";
  if (status === "SUSPENDED") return "text-amber-400";
  return "text-red-400";
}

function statusBg(status: string) {
  if (status === "ACTIVE") return "bg-emerald-500/10 border-emerald-500/20";
  if (status === "SUSPENDED") return "bg-amber-500/10 border-amber-500/20";
  return "bg-red-500/10 border-red-500/20";
}

function StatusIcon({ status }: { status: string }) {
  if (status === "ACTIVE") return <CheckCircle className="size-3.5 text-emerald-400" />;
  if (status === "SUSPENDED") return <AlertTriangle className="size-3.5 text-amber-400" />;
  return <XCircle className="size-3.5 text-red-400" />;
}

// ── Metric pill ───────────────────────────────────────────────────────────────

function Metric({ icon: Icon, value, label }: { icon: React.ElementType; value: number | string; label: string }) {
  return (
    <div className="flex items-center gap-1.5">
      <Icon className="size-3.5 text-muted-foreground shrink-0" />
      <span className="font-mono text-sm font-semibold tabular-nums">{value}</span>
      <span className="text-xs text-muted-foreground">{label}</span>
    </div>
  );
}

// ── Stat header card ──────────────────────────────────────────────────────────

function HeaderStat({ label, value, sub, accent }: { label: string; value: number; sub?: string; accent?: boolean }) {
  return (
    <div className={cn(
      "rounded-xl border p-5 flex flex-col gap-1",
      accent ? "border-accent/30 bg-accent/5" : "border-border bg-card"
    )}>
      <span className="text-xs uppercase tracking-widest text-muted-foreground font-medium">{label}</span>
      <span className={cn("text-3xl font-bold tabular-nums", accent ? "text-accent" : "text-foreground")}>{value}</span>
      {sub && <span className="text-xs text-muted-foreground">{sub}</span>}
    </div>
  );
}

// ── Confirm type-name dialog ───────────────────────────────────────────────────

function DeactivateDialog({
  institution,
  open,
  onClose,
  onConfirm,
  pending,
}: {
  institution: Institution | null;
  open: boolean;
  onClose: () => void;
  onConfirm: () => void;
  pending: boolean;
}) {
  const [typed, setTyped] = useState("");
  const matches = typed === institution?.name;

  return (
    <Dialog open={open} onClose={() => { setTyped(""); onClose(); }} title="Deactivate Tenant" maxWidth="sm">
      <div className="space-y-4">
        <div className="rounded-lg border border-red-500/20 bg-red-500/5 p-4 text-sm text-red-400">
          <div className="flex gap-2">
            <AlertTriangle className="size-4 shrink-0 mt-0.5" />
            <div>
              <p className="font-semibold">This action is irreversible.</p>
              <p className="mt-1 text-red-400/80">
                The tenant will be soft-deactivated. All data is retained for compliance, but all users will lose access immediately.
              </p>
            </div>
          </div>
        </div>
        <Field label={`Type the institution name to confirm`} hint={institution?.name}>
          <Input
            value={typed}
            onChange={(e) => setTyped(e.target.value)}
            placeholder={institution?.name}
            className="font-mono"
          />
        </Field>
        <div className="flex justify-end gap-2">
          <Button variant="ghost" onClick={() => { setTyped(""); onClose(); }}>Cancel</Button>
          <Button
            variant="destructive"
            disabled={!matches || pending}
            onClick={onConfirm}
          >
            {pending && <Loader2 className="size-4 animate-spin" />}
            Deactivate Permanently
          </Button>
        </div>
      </div>
    </Dialog>
  );
}

// ── Main page ─────────────────────────────────────────────────────────────────

type TenantAction = { type: "SUSPEND" | "ACTIVATE" | "DEACTIVATE" | "EDIT"; institution: Institution };

function InstitutionsPage() {
  const qc = useQueryClient();
  const [createOpen, setCreateOpen] = useState(false);
  const [result, setResult] = useState<ProvisionResult | null>(null);
  const [search, setSearch] = useState("");
  const [statusFilter, setStatusFilter] = useState<string>("ALL");
  const [pendingAction, setPendingAction] = useState<TenantAction | null>(null);
  const [selectedTenant, setSelectedTenant] = useState<Institution | null>(null);
  const [editName, setEditName] = useState("");
  const [editType, setEditType] = useState("");
  const [editAddress, setEditAddress] = useState("");

  const instQuery = useQuery({ queryKey: ["institutions"], queryFn: listInstitutions });
  const statsQuery = useQuery({ queryKey: ["tenant-stats"], queryFn: getAllTenantStats });

  const statsMap = useMemo(() => {
    const m = new Map<number, TenantStats>();
    (statsQuery.data ?? []).forEach((s) => m.set(s.institutionId, s));
    return m;
  }, [statsQuery.data]);

  const create = useMutation({
    mutationFn: createInstitution,
    onSuccess: (res) => { setCreateOpen(false); setResult(res); qc.invalidateQueries({ queryKey: ["institutions"] }); qc.invalidateQueries({ queryKey: ["tenant-stats"] }); },
    onError: (e) => toast.error(e instanceof ApiError ? e.detail : "Could not create tenant"),
  });

  const setStatus = useMutation({
    mutationFn: ({ id, suspend }: { id: number; suspend: boolean }) =>
      suspend ? suspendInstitution(id) : activateInstitution(id),
    onSuccess: (inst) => {
      qc.invalidateQueries({ queryKey: ["institutions"] });
      toast.success(`${inst.name} is now ${inst.status}`);
      setPendingAction(null);
    },
    onError: (e) => toast.error(e instanceof ApiError ? e.detail : "Update failed"),
  });

  const deactivate = useMutation({
    mutationFn: (id: number) => deactivateInstitution(id),
    onSuccess: (inst) => {
      qc.invalidateQueries({ queryKey: ["institutions"] });
      qc.invalidateQueries({ queryKey: ["tenant-stats"] });
      toast.success(`${inst.name} has been deactivated`);
      setPendingAction(null);
    },
    onError: (e) => toast.error(e instanceof ApiError ? e.detail : "Deactivation failed"),
  });

  const update = useMutation({
    mutationFn: ({ id, data }: { id: number; data: { name?: string; type?: string; address?: string } }) =>
      updateInstitution(id, data),
    onSuccess: (inst) => {
      qc.invalidateQueries({ queryKey: ["institutions"] });
      qc.invalidateQueries({ queryKey: ["tenant-stats"] });
      toast.success(`${inst.name} updated`);
      setPendingAction(null);
    },
    onError: (e) => toast.error(e instanceof ApiError ? e.detail : "Update failed"),
  });

  // Stats aggregation
  const tenants = instQuery.data ?? [];
  const statsAll = statsQuery.data ?? [];
  const totalStudents = statsAll.reduce((a, s) => a + s.studentCount, 0);
  const totalFaculty = statsAll.reduce((a, s) => a + s.facultyCount, 0);
  const activeCount = tenants.filter((t) => t.status === "ACTIVE").length;
  const suspendedCount = tenants.filter((t) => t.status === "SUSPENDED").length;

  // Filtering
  const filtered = useMemo(() => {
    let rows = tenants;
    if (statusFilter !== "ALL") rows = rows.filter((t) => t.status === statusFilter);
    if (search) {
      const q = search.toLowerCase();
      rows = rows.filter((t) => t.name.toLowerCase().includes(q) || t.code.toLowerCase().includes(q));
    }
    return rows;
  }, [tenants, statusFilter, search]);

  return (
    <div className="space-y-8">
      {/* ── Page header ── */}
      <div className="flex items-start justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">Tenant Management</h1>
          <p className="mt-1 text-sm text-muted-foreground">
            Full lifecycle control for every institution on the platform. Monitor usage, manage access, and provision new tenants.
          </p>
        </div>
        <Button variant="accent" onClick={() => setCreateOpen(true)}>
          <Plus className="size-4" /> New Tenant
        </Button>
      </div>

      {/* ── Summary stats ── */}
      <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        <HeaderStat label="Total Tenants" value={tenants.length} sub={`${activeCount} active`} accent />
        <HeaderStat label="Active" value={activeCount} sub="Live on platform" />
        <HeaderStat label="Suspended" value={suspendedCount} sub="Access revoked" />
        <HeaderStat label="Total Students" value={totalStudents} sub={`across ${tenants.length} tenants`} />
      </div>

      {/* ── Platform-wide usage bar ── */}
      {statsAll.length > 0 && (
        <div className="rounded-xl border border-border bg-card p-5">
          <div className="flex items-center justify-between mb-4">
            <div className="flex items-center gap-2">
              <BarChart3 className="size-4 text-accent" />
              <h2 className="font-semibold text-sm">Platform-Wide Usage</h2>
            </div>
            <Button variant="ghost" size="sm" onClick={() => qc.invalidateQueries({ queryKey: ["tenant-stats"] })}>
              <RefreshCw className="size-3.5 mr-1.5" /> Refresh
            </Button>
          </div>
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-6">
            <div className="text-center">
              <div className="text-3xl font-bold tabular-nums text-accent">{totalStudents.toLocaleString()}</div>
              <div className="text-xs text-muted-foreground mt-1">Total Students</div>
            </div>
            <div className="text-center">
              <div className="text-3xl font-bold tabular-nums">{totalFaculty.toLocaleString()}</div>
              <div className="text-xs text-muted-foreground mt-1">Faculty Members</div>
            </div>
            <div className="text-center">
              <div className="text-3xl font-bold tabular-nums">{statsAll.reduce((a, s) => a + s.totalUsers, 0).toLocaleString()}</div>
              <div className="text-xs text-muted-foreground mt-1">Total Users</div>
            </div>
            <div className="text-center">
              <div className="text-3xl font-bold tabular-nums">{tenants.length}</div>
              <div className="text-xs text-muted-foreground mt-1">Institutions</div>
            </div>
          </div>
        </div>
      )}

      {/* ── Filters ── */}
      <div className="flex flex-col sm:flex-row gap-3">
        <div className="relative flex-1">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 size-4 text-muted-foreground" />
          <Input
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Search by institution name or code…"
            className="pl-9"
          />
        </div>
        <Select value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)} className="w-full sm:w-[180px]">
          <option value="ALL">All Statuses</option>
          <option value="ACTIVE">Active</option>
          <option value="SUSPENDED">Suspended</option>
          <option value="DEACTIVATED">Deactivated</option>
        </Select>
      </div>

      {/* ── Tenant cards grid ── */}
      {instQuery.isLoading ? (
        <div className="flex items-center justify-center py-20 text-muted-foreground">
          <Loader2 className="size-6 animate-spin mr-2" /> Loading tenants…
        </div>
      ) : filtered.length === 0 ? (
        <div className="flex flex-col items-center justify-center py-20 text-center">
          <Building2 className="size-10 text-muted-foreground/30 mb-3" />
          <p className="font-medium text-muted-foreground">No tenants found</p>
          <p className="text-sm text-muted-foreground/60 mt-1">Try adjusting your filters</p>
        </div>
      ) : (
        <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-3">
          {filtered.map((inst) => {
            const stats = statsMap.get(inst.id);
            return (
              <TenantCard
                key={inst.id}
                institution={inst}
                stats={stats}
                onSelect={() => setSelectedTenant(inst)}
                onEdit={() => {
                  setEditName(inst.name);
                  setEditType(inst.type);
                  setEditAddress(inst.address ?? "");
                  setPendingAction({ type: "EDIT", institution: inst });
                }}
                onSuspend={() => setPendingAction({ type: "SUSPEND", institution: inst })}
                onActivate={() => setPendingAction({ type: "ACTIVATE", institution: inst })}
                onDeactivate={() => setPendingAction({ type: "DEACTIVATE", institution: inst })}
              />
            );
          })}
        </div>
      )}

      {/* ── Tenant Detail Drawer ── */}
      {selectedTenant && (
        <TenantDetailDrawer
          institution={selectedTenant}
          stats={statsMap.get(selectedTenant.id)}
          onClose={() => setSelectedTenant(null)}
          onEdit={() => {
            setEditName(selectedTenant.name);
            setEditType(selectedTenant.type);
            setEditAddress(selectedTenant.address ?? "");
            setPendingAction({ type: "EDIT", institution: selectedTenant });
            setSelectedTenant(null);
          }}
          onSuspend={() => { setPendingAction({ type: "SUSPEND", institution: selectedTenant }); setSelectedTenant(null); }}
          onActivate={() => { setPendingAction({ type: "ACTIVATE", institution: selectedTenant }); setSelectedTenant(null); }}
          onDeactivate={() => { setPendingAction({ type: "DEACTIVATE", institution: selectedTenant }); setSelectedTenant(null); }}
        />
      )}

      {/* ── Suspend/Activate confirmation ── */}
      {(pendingAction?.type === "SUSPEND" || pendingAction?.type === "ACTIVATE") && (
        <Dialog
          open
          onClose={() => setPendingAction(null)}
          title={pendingAction.type === "SUSPEND" ? "Suspend Tenant" : "Reactivate Tenant"}
          maxWidth="sm"
        >
          <div className="space-y-4">
            {pendingAction.type === "SUSPEND" && (
              <div className="rounded-lg border border-amber-500/20 bg-amber-500/5 p-3 text-sm text-amber-400">
                All users of <strong>{pendingAction.institution.name}</strong> will immediately lose platform access.
              </div>
            )}
            {pendingAction.type === "ACTIVATE" && (
              <div className="rounded-lg border border-emerald-500/20 bg-emerald-500/5 p-3 text-sm text-emerald-400">
                <strong>{pendingAction.institution.name}</strong> and its users will regain full platform access. A notification will be sent to the institution.
              </div>
            )}
            <div className="flex justify-end gap-2">
              <Button variant="ghost" onClick={() => setPendingAction(null)}>Cancel</Button>
              <Button
                variant={pendingAction.type === "SUSPEND" ? "destructive" : "accent"}
                disabled={setStatus.isPending}
                onClick={() => setStatus.mutate({
                  id: pendingAction.institution.id,
                  suspend: pendingAction.type === "SUSPEND",
                })}
              >
                {setStatus.isPending && <Loader2 className="size-4 animate-spin" />}
                {pendingAction.type === "SUSPEND" ? "Suspend" : "Reactivate"}
              </Button>
            </div>
          </div>
        </Dialog>
      )}

      {/* ── Edit tenant ── */}
      {pendingAction?.type === "EDIT" && (
        <Dialog open onClose={() => setPendingAction(null)} title="Edit Institution Details" maxWidth="sm">
          <form
            className="space-y-4"
            onSubmit={(e) => {
              e.preventDefault();
              update.mutate({
                id: pendingAction.institution.id,
                data: { name: editName, type: editType, address: editAddress },
              });
            }}
          >
            <Field label="Institution name">
              <Input value={editName} onChange={(e) => setEditName(e.target.value)} required />
            </Field>
            <Field label="Type">
              <Select value={editType} onChange={(e) => setEditType(e.target.value)}>
                <option value="UNIVERSITY">University</option>
                <option value="COLLEGE">College</option>
                <option value="INSTITUTE">Institute</option>
              </Select>
            </Field>
            <Field label="Address" hint="Optional">
              <Input value={editAddress} onChange={(e) => setEditAddress(e.target.value)} placeholder="123 Campus Road, City" />
            </Field>
            <div className="flex justify-end gap-2 pt-1">
              <Button type="button" variant="ghost" onClick={() => setPendingAction(null)}>Cancel</Button>
              <Button type="submit" variant="accent" disabled={update.isPending}>
                {update.isPending && <Loader2 className="size-4 animate-spin" />}
                Save changes
              </Button>
            </div>
          </form>
        </Dialog>
      )}

      {/* ── Deactivate confirmation ── */}
      <DeactivateDialog
        institution={pendingAction?.type === "DEACTIVATE" ? pendingAction.institution : null}
        open={pendingAction?.type === "DEACTIVATE"}
        onClose={() => setPendingAction(null)}
        onConfirm={() => {
          if (pendingAction?.institution) deactivate.mutate(pendingAction.institution.id);
        }}
        pending={deactivate.isPending}
      />

      {/* ── Create tenant ── */}
      <Dialog open={createOpen} onClose={() => setCreateOpen(false)} title="Provision New Tenant" description="Creates the institution and its first admin immediately. A welcome email with credentials will be sent.">
        <form
          onSubmit={(e) => {
            e.preventDefault();
            const f = new FormData(e.currentTarget);
            create.mutate({
              institutionName: f.get("institutionName") as string,
              institutionCode: f.get("institutionCode") as string,
              type: f.get("type") as string,
              adminFullName: f.get("adminFullName") as string,
              adminEmail: f.get("adminEmail") as string,
              password: (f.get("password") as string) || undefined,
            });
          }}
          className="space-y-4"
        >
          <div className="grid gap-4 sm:grid-cols-2">
            <Field label="Institution name"><Input name="institutionName" required /></Field>
            <Field label="Tenant code"><Input name="institutionCode" required className="font-mono uppercase" /></Field>
          </div>
          <Field label="Type">
            <Select name="type" defaultValue="UNIVERSITY">
              <option value="UNIVERSITY">University</option>
              <option value="COLLEGE">College</option>
              <option value="INSTITUTE">Institute</option>
            </Select>
          </Field>
          <div className="grid gap-4 sm:grid-cols-2">
            <Field label="Admin name"><Input name="adminFullName" required /></Field>
            <Field label="Admin email"><Input name="adminEmail" type="email" required /></Field>
          </div>
          <Field label="Admin password" hint="Leave blank to auto-generate. Admin will receive credentials by email.">
            <Input name="password" type="password" placeholder="Auto-generate" />
          </Field>
          <div className="flex justify-end gap-2">
            <Button type="button" variant="ghost" onClick={() => setCreateOpen(false)}>Cancel</Button>
            <Button type="submit" variant="accent" disabled={create.isPending}>
              {create.isPending && <Loader2 className="size-4 animate-spin" />}
              Provision Tenant
            </Button>
          </div>
        </form>
      </Dialog>

      {/* ── Provision result ── */}
      <Dialog open={!!result} onClose={() => setResult(null)} title="Tenant Provisioned Successfully">
        {result && (
          <div className="space-y-4">
            <div className="rounded-lg border border-emerald-500/20 bg-emerald-500/5 p-3 text-sm text-emerald-400 flex gap-2">
              <CheckCircle className="size-4 shrink-0 mt-0.5" />
              The institution has been provisioned and the admin has received a welcome email with login credentials.
            </div>
            <div className="space-y-2 rounded-lg border border-border bg-muted/40 p-4 text-sm">
              <DetailRow label="Tenant code" value={result.institutionCode} mono />
              <DetailRow label="Admin email" value={result.adminEmail} />
              {result.temporaryPassword && (
                <div className="flex items-center justify-between gap-2">
                  <div className="flex flex-col gap-0.5">
                    <span className="text-xs uppercase tracking-wide text-muted-foreground">Temp password</span>
                    <span className="font-mono text-sm">{result.temporaryPassword}</span>
                  </div>
                  <Button
                    variant="ghost" size="sm"
                    onClick={() => { navigator.clipboard.writeText(result.temporaryPassword!); toast.success("Copied"); }}
                  >
                    <Copy className="size-4" />
                  </Button>
                </div>
              )}
            </div>
            <p className="text-xs text-muted-foreground">Share these credentials securely. The admin must change the password on first sign-in.</p>
            <div className="flex justify-end">
              <Button variant="accent" onClick={() => setResult(null)}>Done</Button>
            </div>
          </div>
        )}
      </Dialog>
    </div>
  );
}

// ── Tenant Card ───────────────────────────────────────────────────────────────

function TenantCard({
  institution: inst,
  stats,
  onSelect,
  onEdit,
  onSuspend,
  onActivate,
  onDeactivate,
}: {
  institution: Institution;
  stats?: TenantStats;
  onSelect: () => void;
  onEdit: () => void;
  onSuspend: () => void;
  onActivate: () => void;
  onDeactivate: () => void;
}) {
  return (
    <div
      className={cn(
        "group relative rounded-xl border bg-card transition-all duration-200 cursor-pointer",
        "hover:border-accent/40 hover:shadow-lg hover:shadow-accent/5",
        statusBg(inst.status)
      )}
      onClick={onSelect}
    >
      {/* Status strip */}
      <div className={cn(
        "absolute top-0 left-0 right-0 h-0.5 rounded-t-xl",
        inst.status === "ACTIVE" ? "bg-emerald-500" : inst.status === "SUSPENDED" ? "bg-amber-500" : "bg-red-500"
      )} />

      <div className="p-5 space-y-4">
        {/* Header */}
        <div className="flex items-start justify-between gap-3">
          <div className="flex items-center gap-3 min-w-0">
            <div className="flex size-10 shrink-0 items-center justify-center rounded-lg border border-border bg-muted/50">
              <Building2 className="size-5 text-muted-foreground" />
            </div>
            <div className="min-w-0">
              <p className="font-semibold truncate leading-tight">{inst.name}</p>
              <p className="font-mono text-xs uppercase text-muted-foreground">{inst.code} · {inst.type}</p>
            </div>
          </div>
          <div className={cn(
            "flex items-center gap-1.5 shrink-0 rounded-full border px-2.5 py-1 text-xs font-medium",
            statusBg(inst.status), statusColor(inst.status)
          )}>
            <StatusIcon status={inst.status} />
            {inst.status}
          </div>
        </div>

        {/* Usage metrics */}
        {stats ? (
          <div className="grid grid-cols-3 gap-3 rounded-lg border border-border/50 bg-muted/30 p-3">
            <Metric icon={GraduationCap} value={stats.studentCount.toLocaleString()} label="students" />
            <Metric icon={BookOpen} value={stats.facultyCount.toLocaleString()} label="faculty" />
            <Metric icon={Users} value={stats.totalUsers.toLocaleString()} label="users" />
          </div>
        ) : (
          <div className="grid grid-cols-3 gap-3 rounded-lg border border-border/50 bg-muted/30 p-3">
            {[0, 1, 2].map((i) => (
              <div key={i} className="h-4 rounded bg-muted animate-pulse" />
            ))}
          </div>
        )}

        {/* Footer */}
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-1.5 text-xs text-muted-foreground">
            <Calendar className="size-3" />
            {inst.createdAt ? shortDate(inst.createdAt) : "—"}
          </div>
          <div
            className="flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity"
            onClick={(e) => e.stopPropagation()}
          >
            <Button variant="ghost" size="sm" className="h-7 px-2 text-xs" onClick={onEdit}>
              <Pencil className="size-3" />
            </Button>
            {inst.status === "ACTIVE" && (
              <Button variant="ghost" size="sm" className="h-7 px-2 text-xs text-amber-400 hover:text-amber-300" onClick={onSuspend}>
                <PowerOff className="size-3" />
              </Button>
            )}
            {inst.status === "SUSPENDED" && (
              <Button variant="ghost" size="sm" className="h-7 px-2 text-xs text-emerald-400 hover:text-emerald-300" onClick={onActivate}>
                <Power className="size-3" />
              </Button>
            )}
            {inst.status !== "DEACTIVATED" && (
              <Button variant="ghost" size="sm" className="h-7 px-2 text-xs text-red-400 hover:text-red-300" onClick={onDeactivate}>
                <Trash2 className="size-3" />
              </Button>
            )}
          </div>
        </div>
      </div>

      {/* "View details" hint */}
      <div className="flex items-center justify-center gap-1 border-t border-border/50 py-2 text-xs text-muted-foreground group-hover:text-accent transition-colors">
        View details <ChevronRight className="size-3" />
      </div>
    </div>
  );
}

// ── Tenant Detail Drawer ──────────────────────────────────────────────────────

function TenantDetailDrawer({
  institution: inst,
  stats,
  onClose,
  onEdit,
  onSuspend,
  onActivate,
  onDeactivate,
}: {
  institution: Institution;
  stats?: TenantStats;
  onClose: () => void;
  onEdit: () => void;
  onSuspend: () => void;
  onActivate: () => void;
  onDeactivate: () => void;
}) {
  return (
    <>
      {/* Backdrop */}
      <div
        className="fixed inset-0 z-40 bg-black/60 backdrop-blur-sm"
        onClick={onClose}
      />
      {/* Drawer */}
      <div className="fixed right-0 top-0 bottom-0 z-50 w-full sm:w-[480px] bg-card border-l border-border shadow-2xl overflow-y-auto">
        <div className="sticky top-0 z-10 bg-card/95 backdrop-blur border-b border-border p-5 flex items-center justify-between">
          <div>
            <h2 className="font-bold text-lg">{inst.name}</h2>
            <p className="font-mono text-xs uppercase text-muted-foreground">{inst.code}</p>
          </div>
          <Button variant="ghost" size="sm" onClick={onClose} className="rounded-full">✕</Button>
        </div>

        <div className="p-5 space-y-6">
          {/* Status badge */}
          <div className={cn(
            "flex items-center gap-2 rounded-lg border px-4 py-3",
            statusBg(inst.status)
          )}>
            <StatusIcon status={inst.status} />
            <span className={cn("font-semibold text-sm", statusColor(inst.status))}>{inst.status}</span>
          </div>

          {/* Usage stats */}
          {stats && (
            <div>
              <h3 className="text-xs uppercase tracking-widest text-muted-foreground font-medium mb-3">Usage Statistics</h3>
              <div className="grid grid-cols-2 gap-3">
                {[
                  { label: "Students", value: stats.studentCount, icon: GraduationCap },
                  { label: "Faculty", value: stats.facultyCount, icon: BookOpen },
                  { label: "Total Users", value: stats.totalUsers, icon: Users },
                  { label: "HODs", value: stats.hodCount, icon: Users },
                  { label: "Placement Officers", value: stats.placementOfficerCount, icon: Users },
                  { label: "Finance Staff", value: stats.financeCount, icon: Users },
                ].map(({ label, value, icon: Icon }) => (
                  <div key={label} className="rounded-lg border border-border bg-muted/30 p-3 flex items-center gap-3">
                    <Icon className="size-4 text-accent shrink-0" />
                    <div>
                      <div className="text-xl font-bold tabular-nums">{value.toLocaleString()}</div>
                      <div className="text-xs text-muted-foreground">{label}</div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* Details */}
          <div>
            <h3 className="text-xs uppercase tracking-widest text-muted-foreground font-medium mb-3">Institution Details</h3>
            <div className="rounded-lg border border-border bg-muted/30 divide-y divide-border">
              <DetailRow label="Institution name" value={inst.name} />
              <DetailRow label="Tenant code" value={inst.code} mono />
              <DetailRow label="Type" value={inst.type} />
              {inst.address && (
                <div className="flex items-start gap-2 px-4 py-3">
                  <MapPin className="size-3.5 mt-0.5 text-muted-foreground shrink-0" />
                  <span className="text-sm">{inst.address}</span>
                </div>
              )}
              {inst.createdAt && <DetailRow label="Provisioned" value={shortDate(inst.createdAt)} />}
            </div>
          </div>

          {/* Actions */}
          <div>
            <h3 className="text-xs uppercase tracking-widest text-muted-foreground font-medium mb-3">Actions</h3>
            <div className="space-y-2">
              <Button variant="outline" className="w-full justify-start" onClick={onEdit}>
                <Pencil className="size-4" /> Edit Institution Details
              </Button>
              {inst.status === "ACTIVE" && (
                <Button variant="outline" className="w-full justify-start text-amber-400 border-amber-500/30 hover:border-amber-500/60" onClick={onSuspend}>
                  <PowerOff className="size-4" /> Suspend Tenant
                </Button>
              )}
              {inst.status === "SUSPENDED" && (
                <Button variant="outline" className="w-full justify-start text-emerald-400 border-emerald-500/30 hover:border-emerald-500/60" onClick={onActivate}>
                  <Power className="size-4" /> Reactivate Tenant
                </Button>
              )}
              {inst.status !== "DEACTIVATED" && (
                <Button variant="outline" className="w-full justify-start text-red-400 border-red-500/30 hover:border-red-500/60" onClick={onDeactivate}>
                  <Trash2 className="size-4" /> Deactivate Tenant
                </Button>
              )}
            </div>
          </div>
        </div>
      </div>
    </>
  );
}

// ── Detail Row helper ─────────────────────────────────────────────────────────

function DetailRow({ label, value, mono }: { label: string; value: string; mono?: boolean }) {
  return (
    <div className="flex items-center justify-between gap-4 px-4 py-3">
      <span className="text-xs uppercase tracking-wide text-muted-foreground shrink-0">{label}</span>
      <span className={cn("text-sm font-medium text-right", mono && "font-mono uppercase")}>{value}</span>
    </div>
  );
}
