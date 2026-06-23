import { useState } from "react";
import { createFileRoute, Link } from "@tanstack/react-router";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { Plus, Loader2, Settings2, TrendingUp, Trophy, Building, FileText } from "lucide-react";
import { toast } from "sonner";
import {
  listPostings, createPosting, listCompanies, createCompany, placementStats,
} from "@/lib/api/placement";
import type { EligibilityCriteria } from "@/lib/api/entities";
import { ApiError } from "@/lib/api/client";
import { PageHeader, TabBar, DataState, StatCard, moneyINR } from "@/components/common";
import { StatusBadge } from "@/components/ui/badge";
import { DataTable, Row, Cell } from "@/components/ui/table";
import { Dialog } from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Input, Textarea } from "@/components/ui/input";
import { Select } from "@/components/ui/select";
import { Field } from "@/components/ui/field";

export const Route = createFileRoute("/_authenticated/institution/placement")({
  component: PlacementPage,
});

type Tab = "postings" | "companies" | "stats";
const errToast = (e: unknown) => toast.error(e instanceof ApiError ? e.detail : "Action failed");

function PlacementPage() {
  const [tab, setTab] = useState<Tab>("postings");
  return (
    <div>
      <PageHeader
        title="Placements"
        description="Companies, postings and recruitment analytics."
        actions={
          <TabBar<Tab>
            value={tab}
            onChange={setTab}
            tabs={[
              { value: "postings", label: "Postings" },
              { value: "companies", label: "Companies" },
              { value: "stats", label: "Analytics" },
            ]}
          />
        }
      />
      {tab === "postings" && <PostingsTab />}
      {tab === "companies" && <CompaniesTab />}
      {tab === "stats" && <StatsTab />}
    </div>
  );
}

function PostingsTab() {
  const qc = useQueryClient();
  const [open, setOpen] = useState(false);
  const q = useQuery({ queryKey: ["postings", "all"], queryFn: () => listPostings(false) });
  const companies = useQuery({ queryKey: ["companies"], queryFn: listCompanies });
  const m = useMutation({
    mutationFn: createPosting,
    onSuccess: () => { setOpen(false); qc.invalidateQueries({ queryKey: ["postings"] }); toast.success("Posting created"); },
    onError: errToast,
  });
  const rows = q.data ?? [];
  const companyName = (id: number) => companies.data?.find((c) => c.id === id)?.name ?? `#${id}`;

  return (
    <div>
      <div className="mb-4 flex justify-end">
        <Button variant="accent" onClick={() => setOpen(true)}>
          <Plus className="size-4" /> New posting
        </Button>
      </div>
      <DataState isLoading={q.isLoading} error={q.error} isEmpty={rows.length === 0} emptyTitle="No postings" emptyBody="Create a posting with eligibility rules.">
        <DataTable columns={["Role", "Company", "Type", "CTC", "Status", ""]}>
          {rows.map((p, i) => (
            <Row key={p.id} index={i}>
              <Cell className="font-medium">{p.title}</Cell>
              <Cell>{companyName(p.companyId)}</Cell>
              <Cell className="text-muted-foreground">{p.type}</Cell>
              <Cell className="font-mono">{moneyINR(p.ctc)}</Cell>
              <Cell><StatusBadge status={p.status} /></Cell>
              <Cell>
                <div className="flex justify-end">
                  <Link to="/institution/posting/$postingId" params={{ postingId: String(p.id) }}>
                    <Button variant="outline" size="sm">
                      <Settings2 className="size-3.5" /> Manage
                    </Button>
                  </Link>
                </div>
              </Cell>
            </Row>
          ))}
        </DataTable>
      </DataState>

      <Dialog open={open} onClose={() => setOpen(false)} title="New posting" description="Define the role and eligibility rules.">
        <form
          onSubmit={(e) => {
            e.preventDefault();
            const f = new FormData(e.currentTarget);
            const branches = (f.get("branches") as string).split(",").map((b) => b.trim()).filter(Boolean);
            const eligibility: EligibilityCriteria = {};
            if (f.get("minCgpa")) eligibility.minCgpa = Number(f.get("minCgpa"));
            if (branches.length) eligibility.branches = branches;
            if (f.get("maxBacklogs")) eligibility.maxBacklogs = Number(f.get("maxBacklogs"));
            if (f.get("batchYear")) eligibility.batchYear = Number(f.get("batchYear"));
            m.mutate({
              companyId: Number(f.get("companyId")),
              title: f.get("title") as string,
              type: f.get("type") as string,
              ctc: f.get("ctc") ? Number(f.get("ctc")) : undefined,
              location: (f.get("location") as string) || undefined,
              description: (f.get("description") as string) || undefined,
              eligibility: Object.keys(eligibility).length ? eligibility : undefined,
            });
          }}
          className="space-y-4"
        >
          <Field label="Company"><Select name="companyId" required>{(companies.data ?? []).map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}</Select></Field>
          <div className="grid gap-4 sm:grid-cols-2">
            <Field label="Role title"><Input name="title" required /></Field>
            <Field label="Type"><Select name="type" defaultValue="FULL_TIME"><option value="FULL_TIME">Full-time</option><option value="INTERNSHIP">Internship</option><option value="PPO">PPO</option></Select></Field>
          </div>
          <div className="grid gap-4 sm:grid-cols-2">
            <Field label="CTC (₹/yr)"><Input name="ctc" type="number" placeholder="1800000" /></Field>
            <Field label="Location"><Input name="location" placeholder="Bengaluru" /></Field>
          </div>
          <Field label="Description" hint="Optional"><Textarea name="description" /></Field>

          <div className="rounded-lg border border-border bg-muted/30 p-4">
            <p className="mb-3 text-xs font-semibold uppercase tracking-wide text-accent">Eligibility rules</p>
            <div className="grid gap-4 sm:grid-cols-2">
              <Field label="Min CGPA"><Input name="minCgpa" type="number" step="0.1" placeholder="7.0" /></Field>
              <Field label="Max backlogs"><Input name="maxBacklogs" type="number" placeholder="0" /></Field>
              <Field label="Batch year"><Input name="batchYear" type="number" placeholder="2026" /></Field>
              <Field label="Branches" hint="Comma-separated"><Input name="branches" placeholder="CSE, ECE" className="font-mono" /></Field>
            </div>
          </div>

          <div className="flex justify-end gap-2 pt-1">
            <Button type="button" variant="ghost" onClick={() => setOpen(false)}>Cancel</Button>
            <Button type="submit" variant="accent" disabled={m.isPending}>
              {m.isPending && <Loader2 className="size-4 animate-spin" />}
              Create posting
            </Button>
          </div>
        </form>
      </Dialog>
    </div>
  );
}

function CompaniesTab() {
  const qc = useQueryClient();
  const [open, setOpen] = useState(false);
  const q = useQuery({ queryKey: ["companies"], queryFn: listCompanies });
  const m = useMutation({
    mutationFn: createCompany,
    onSuccess: () => { setOpen(false); qc.invalidateQueries({ queryKey: ["companies"] }); toast.success("Company added"); },
    onError: errToast,
  });
  const rows = q.data ?? [];
  return (
    <div>
      <div className="mb-4 flex justify-end">
        <Button variant="accent" onClick={() => setOpen(true)}><Plus className="size-4" /> New company</Button>
      </div>
      <DataState isLoading={q.isLoading} error={q.error} isEmpty={rows.length === 0} emptyTitle="No companies">
        <DataTable columns={["Name", "Sector", "Tier", "Website"]}>
          {rows.map((c, i) => (
            <Row key={c.id} index={i}>
              <Cell className="font-medium">{c.name}</Cell>
              <Cell className="text-muted-foreground">{c.sector ?? "—"}</Cell>
              <Cell>{c.tier ? <StatusBadge status={c.tier} /> : "—"}</Cell>
              <Cell className="font-mono text-xs text-muted-foreground">{c.website ?? "—"}</Cell>
            </Row>
          ))}
        </DataTable>
      </DataState>
      <Dialog open={open} onClose={() => setOpen(false)} title="New company">
        <form onSubmit={(e) => { e.preventDefault(); const f = new FormData(e.currentTarget); m.mutate({ name: f.get("name") as string, sector: (f.get("sector") as string) || undefined, tier: f.get("tier") as string, website: (f.get("website") as string) || undefined, description: (f.get("description") as string) || undefined }); }} className="space-y-4">
          <Field label="Name"><Input name="name" required /></Field>
          <div className="grid gap-4 sm:grid-cols-2">
            <Field label="Sector"><Input name="sector" placeholder="Tech" /></Field>
            <Field label="Tier"><Select name="tier" defaultValue="TIER1"><option>DREAM</option><option>TIER1</option><option>TIER2</option><option>MASS</option></Select></Field>
          </div>
          <Field label="Website"><Input name="website" placeholder="https://" /></Field>
          <Field label="Description" hint="Optional"><Textarea name="description" /></Field>
          <div className="flex justify-end gap-2 pt-1">
            <Button type="button" variant="ghost" onClick={() => setOpen(false)}>Cancel</Button>
            <Button type="submit" variant="accent" disabled={m.isPending}>{m.isPending && <Loader2 className="size-4 animate-spin" />}Add</Button>
          </div>
        </form>
      </Dialog>
    </div>
  );
}

function StatsTab() {
  const q = useQuery({ queryKey: ["placementStats"], queryFn: placementStats });
  const s = q.data;
  return (
    <DataState isLoading={q.isLoading} error={q.error}>
      {s && (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          <StatCard label="Placement rate" value={`${s.placementRatePct}%`} icon={TrendingUp} index={0} hint={`${s.placedStudents}/${s.totalStudents} placed`} />
          <StatCard label="Highest CTC" value={moneyINR(s.highestCtc)} icon={Trophy} index={1} />
          <StatCard label="Average CTC" value={moneyINR(s.averageCtc)} icon={TrendingUp} index={2} />
          <StatCard label="Total students" value={s.totalStudents} icon={Building} index={3} />
          <StatCard label="Open postings" value={s.openPostings} icon={FileText} index={4} />
          <StatCard label="Total offers" value={s.totalOffers} icon={Trophy} index={5} />
        </div>
      )}
    </DataState>
  );
}
