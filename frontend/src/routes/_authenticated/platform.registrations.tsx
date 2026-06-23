import { useState, useMemo } from "react";
import { createFileRoute } from "@tanstack/react-router";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { Check, X, Copy, KeyRound, Loader2 } from "lucide-react";
import { toast } from "sonner";
import {
  listRegistrations,
  approveRegistration,
  rejectRegistration,
} from "@/lib/api/platform";
import type { Registration, RegistrationStatus, ProvisionResult } from "@/lib/api/entities";
import { ApiError } from "@/lib/api/client";
import { PageHeader, DataState, shortDate } from "@/components/common";
import { StatusBadge } from "@/components/ui/badge";
import { Dialog } from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Textarea } from "@/components/ui/input";
import { Field } from "@/components/ui/field";
import { Select } from "@/components/ui/select";
import {
  EnterpriseDataTable,
  ColumnDef,
  FilterBar,
  DetailDrawer,
  FormActions,
  PaginationState
} from "@/components/enterprise";

export const Route = createFileRoute("/_authenticated/platform/registrations")({
  component: RegistrationsPage,
});

type Filter = "ALL" | RegistrationStatus;

function RegistrationsPage() {
  const qc = useQueryClient();
  const [filter, setFilter] = useState<Filter>("PENDING");
  const [search, setSearch] = useState("");
  const [pageState, setPageState] = useState<PaginationState>({ page: 0, size: 10 });
  const [selectedReg, setSelectedReg] = useState<Registration | null>(null);
  
  const [approveOpen, setApproveOpen] = useState(false);
  const [rejectOpen, setRejectOpen] = useState(false);
  const [result, setResult] = useState<ProvisionResult | null>(null);

  const query = useQuery({
    queryKey: ["registrations", filter],
    queryFn: () => listRegistrations(filter === "ALL" ? undefined : filter),
  });

  const approve = useMutation({
    mutationFn: ({ id, notes }: { id: number; notes: string }) => approveRegistration(id, notes),
    onSuccess: (res) => {
      setApproveOpen(false);
      setSelectedReg(null);
      setResult(res);
      qc.invalidateQueries({ queryKey: ["registrations"] });
      qc.invalidateQueries({ queryKey: ["institutions"] });
    },
    onError: (e) => toast.error(e instanceof ApiError ? e.detail : "Approve failed"),
  });

  const reject = useMutation({
    mutationFn: ({ id, notes }: { id: number; notes: string }) => rejectRegistration(id, notes),
    onSuccess: () => {
      setRejectOpen(false);
      setSelectedReg(null);
      qc.invalidateQueries({ queryKey: ["registrations"] });
      toast.success("Registration rejected");
    },
    onError: (e) => toast.error(e instanceof ApiError ? e.detail : "Reject failed"),
  });

  // Client-side filtering and pagination
  const filteredRows = useMemo(() => {
    let rows = query.data ?? [];
    if (search) {
      const lower = search.toLowerCase();
      rows = rows.filter(r => 
        r.institutionName.toLowerCase().includes(lower) || 
        r.institutionCode.toLowerCase().includes(lower) ||
        r.adminEmail.toLowerCase().includes(lower)
      );
    }
    return rows;
  }, [query.data, search]);

  const totalPages = Math.ceil(filteredRows.length / pageState.size);
  const pagedRows = filteredRows.slice(pageState.page * pageState.size, (pageState.page + 1) * pageState.size);

  const columns: ColumnDef<Registration>[] = [
    {
      header: "Institution",
      cell: (r) => (
        <div>
          <div className="font-medium">{r.institutionName}</div>
          <div className="text-xs text-muted-foreground">{r.type}</div>
        </div>
      )
    },
    {
      header: "Code",
      className: "font-mono text-xs uppercase",
      accessorKey: "institutionCode"
    },
    {
      header: "Admin",
      cell: (r) => (
        <div>
          <div>{r.adminFullName}</div>
          <div className="text-xs text-muted-foreground">{r.adminEmail}</div>
        </div>
      )
    },
    {
      header: "Status",
      cell: (r) => <StatusBadge status={r.status} />
    },
    {
      header: "Submitted",
      className: "whitespace-nowrap text-xs text-muted-foreground",
      cell: (r) => shortDate(r.createdAt)
    }
  ];

  return (
    <div>
      <PageHeader
        title="Registration requests"
        description="Review institution applications and provision approved tenants."
      />

      <div className="mb-6">
        <FilterBar
          onSearchChange={setSearch}
          searchPlaceholder="Search institutions or emails..."
          filters={
            <Select 
              value={filter} 
              onChange={(e) => { setFilter(e.target.value as Filter); setPageState(p => ({ ...p, page: 0 })); }}
              className="w-[180px]"
            >
              <option value="PENDING">Status: Pending</option>
              <option value="APPROVED">Status: Approved</option>
              <option value="REJECTED">Status: Rejected</option>
              <option value="ALL">Status: All</option>
            </Select>
          }
        />
      </div>

      <EnterpriseDataTable
        data={pagedRows}
        columns={columns}
        keyExtractor={(r) => r.id}
        isLoading={query.isLoading}
        error={query.error}
        emptyTitle="No requests found"
        emptyBody="There are no registration requests matching your filters."
        onRowClick={(r) => setSelectedReg(r)}
        pagination={{
          state: pageState,
          totalPages: totalPages,
          totalElements: filteredRows.length,
          onPageChange: (p) => setPageState(prev => ({ ...prev, page: p }))
        }}
      />

      {/* Detail Drawer */}
      <DetailDrawer
        open={!!selectedReg && !approveOpen && !rejectOpen && !result}
        onClose={() => setSelectedReg(null)}
        title={selectedReg?.institutionName || "Registration Details"}
        subtitle={`Submitted on ${shortDate(selectedReg?.createdAt)}`}
        actions={
          selectedReg?.status === "PENDING" ? (
            <FormActions className="w-full mt-0 border-0 pt-0 pb-0 px-0">
              <Button variant="outline" onClick={() => setRejectOpen(true)}>
                <X className="mr-2 h-4 w-4" /> Reject
              </Button>
              <Button variant="accent" onClick={() => setApproveOpen(true)}>
                <Check className="mr-2 h-4 w-4" /> Approve & Provision
              </Button>
            </FormActions>
          ) : undefined
        }
      >
        {selectedReg && (
          <div className="space-y-6">
            <div>
              <h4 className="text-sm font-medium text-foreground mb-3">Institution Details</h4>
              <div className="grid grid-cols-2 gap-4 rounded-lg border border-border bg-muted/40 p-4">
                <Detail label="Name" value={selectedReg.institutionName} />
                <Detail label="Code" value={selectedReg.institutionCode} mono />
                <Detail label="Type" value={selectedReg.type} />
                <Detail label="Status" value={selectedReg.status} />
              </div>
            </div>
            
            <div>
              <h4 className="text-sm font-medium text-foreground mb-3">Administrator Information</h4>
              <div className="grid grid-cols-2 gap-4 rounded-lg border border-border bg-muted/40 p-4">
                <Detail label="Full Name" value={selectedReg.adminFullName} />
                <Detail label="Email Address" value={selectedReg.adminEmail} />
              </div>
            </div>

            {(selectedReg.status === "APPROVED" || selectedReg.status === "REJECTED") && selectedReg.reviewNotes && (
              <div>
                <h4 className="text-sm font-medium text-foreground mb-3">Review Notes</h4>
                <div className="rounded-lg border border-border p-4 text-sm bg-card text-muted-foreground whitespace-pre-wrap">
                  {selectedReg.reviewNotes}
                </div>
              </div>
            )}
          </div>
        )}
      </DetailDrawer>

      {/* Approve dialog */}
      <Dialog
        open={approveOpen}
        onClose={() => setApproveOpen(false)}
        title="Approve & provision tenant"
        description={selectedReg?.institutionName}
      >
        <form
          onSubmit={(e) => {
            e.preventDefault();
            if (!selectedReg) return;
            const notes = (new FormData(e.currentTarget).get("notes") as string) ?? "";
            approve.mutate({ id: selectedReg.id, notes });
          }}
          className="space-y-5"
        >
          <p className="text-sm text-muted-foreground">
            This creates the institution and its first admin. A one-time temporary password will be
            generated.
          </p>
          <Field label="Review notes" hint="Optional">
            <Textarea name="notes" placeholder="Verified accreditation, etc." />
          </Field>
          <div className="flex justify-end gap-2">
            <Button type="button" variant="ghost" onClick={() => setApproveOpen(false)}>
              Cancel
            </Button>
            <Button type="submit" variant="accent" disabled={approve.isPending}>
              {approve.isPending && <Loader2 className="size-4 animate-spin" />}
              Approve & provision
            </Button>
          </div>
        </form>
      </Dialog>

      {/* Reject dialog */}
      <Dialog
        open={rejectOpen}
        onClose={() => setRejectOpen(false)}
        title="Reject registration"
        description={selectedReg?.institutionName}
      >
        <form
          onSubmit={(e) => {
            e.preventDefault();
            if (!selectedReg) return;
            const notes = (new FormData(e.currentTarget).get("notes") as string) ?? "";
            reject.mutate({ id: selectedReg.id, notes });
          }}
          className="space-y-5"
        >
          <Field label="Reason" hint="Shared with the applicant">
            <Textarea name="notes" placeholder="Reason for rejection…" />
          </Field>
          <div className="flex justify-end gap-2">
            <Button type="button" variant="ghost" onClick={() => setRejectOpen(false)}>
              Cancel
            </Button>
            <Button type="submit" variant="destructive" disabled={reject.isPending}>
              {reject.isPending && <Loader2 className="size-4 animate-spin" />}
              Reject
            </Button>
          </div>
        </form>
      </Dialog>

      {/* Provision result */}
      <Dialog
        open={!!result}
        onClose={() => setResult(null)}
        title="Tenant provisioned"
        description="Share these credentials securely with the institution admin."
      >
        {result && (
          <div className="space-y-4">
            <div className="grid gap-2 rounded-lg border border-border bg-muted/40 p-4 text-sm">
              <Detail label="Tenant code" value={result.institutionCode} mono />
              <Detail label="Admin email" value={result.adminEmail} />
              {result.temporaryPassword && (
                <div className="flex items-center justify-between gap-2">
                  <div className="flex items-center gap-2">
                    <KeyRound className="size-4 text-accent" />
                    <span className="font-mono text-sm">{result.temporaryPassword}</span>
                  </div>
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={() => {
                      navigator.clipboard.writeText(result.temporaryPassword!);
                      toast.success("Copied");
                    }}
                  >
                    <Copy className="size-4" /> Copy
                  </Button>
                </div>
              )}
            </div>
            <p className="text-xs text-muted-foreground">
              The admin must change this password on first sign-in.
            </p>
            <div className="flex justify-end">
              <Button variant="accent" onClick={() => setResult(null)}>
                Done
              </Button>
            </div>
          </div>
        )}
      </Dialog>
    </div>
  );
}

function Detail({ label, value, mono }: { label: string; value: string; mono?: boolean }) {
  return (
    <div className="flex flex-col gap-1">
      <span className="text-xs uppercase tracking-wide text-muted-foreground">{label}</span>
      <span className={mono ? "font-mono text-sm uppercase" : "text-sm font-medium"}>{value}</span>
    </div>
  );
}
