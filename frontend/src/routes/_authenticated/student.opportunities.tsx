import { useState } from "react";
import { createFileRoute } from "@tanstack/react-router";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Briefcase, CheckCircle2, Loader2, MapPin, Sparkles } from "lucide-react";
import { toast } from "sonner";
import { jobFit } from "@/lib/api/ai";
import { ApiError } from "@/lib/api/client";
import type { EligibilityCriteria, JobFitReport, JobPosting } from "@/lib/api/entities";
import { applyToPosting, listCompanies, listPostings } from "@/lib/api/placement";
import { getMyProfile } from "@/lib/api/students";
import { DataState, PageHeader, moneyINR, shortDate } from "@/components/common";
import { Badge, StatusBadge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Dialog } from "@/components/ui/dialog";

export const Route = createFileRoute("/_authenticated/student/opportunities")({
  component: OpportunitiesPage,
});

function OpportunitiesPage() {
  const qc = useQueryClient();
  const [fitFor, setFitFor] = useState<JobPosting | null>(null);
  const [fitReport, setFitReport] = useState<JobFitReport | null>(null);

  const me = useQuery({ queryKey: ["myProfile"], queryFn: getMyProfile });
  const postings = useQuery({ queryKey: ["postings", "open"], queryFn: () => listPostings(true) });
  const companies = useQuery({ queryKey: ["companies"], queryFn: listCompanies });

  const apply = useMutation({
    mutationFn: (postingId: number) => applyToPosting(postingId, me.data!.id),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["myApplications"] });
      toast.success("Application submitted");
    },
    onError: (e) => {
      toast.error(e instanceof ApiError ? e.detail : "Unable to apply");
    },
  });

  const fit = useMutation({
    mutationFn: (postingId: number) => jobFit(me.data!.id, postingId),
    onSuccess: (report) => setFitReport(report),
    onError: (e) => {
      toast.error(e instanceof ApiError ? e.detail : "Unable to check fit");
    },
  });

  const rows = postings.data ?? [];
  const companyName = (id: number) => companies.data?.find((c) => c.id === id)?.name ?? `#${id}`;

  function onCheckFit(posting: JobPosting) {
    setFitFor(posting);
    setFitReport(null);
    fit.mutate(posting.id);
  }

  return (
    <div>
      <PageHeader
        title="Opportunities"
        description="Open postings you can review, fit-check, and apply to."
      />

      <DataState
        isLoading={postings.isLoading || me.isLoading}
        error={postings.error ?? me.error}
        isEmpty={rows.length === 0}
        emptyTitle="No open opportunities"
        emptyBody="New postings will appear here when the placement cell publishes them."
      >
        <div className="grid gap-4 xl:grid-cols-2">
          {rows.map((posting, index) => {
            const criteria = parseEligibility(posting.eligibility);
            return (
              <Card key={posting.id} className="transition-colors hover:border-accent/40">
                <CardContent className="flex h-full flex-col p-6">
                  <div className="flex flex-wrap items-start justify-between gap-4">
                    <div className="min-w-0">
                      <div className="flex flex-wrap items-center gap-2">
                        <h2 className="text-lg font-semibold">{posting.title}</h2>
                        <StatusBadge status={posting.status} />
                      </div>
                      <p className="mt-1 text-sm text-muted-foreground">
                        {companyName(posting.companyId)} / {posting.type}
                      </p>
                    </div>
                    <div className="font-mono text-lg font-semibold">{moneyINR(posting.ctc)}</div>
                  </div>

                  <div className="mt-4 flex flex-wrap gap-2 text-sm text-muted-foreground">
                    <span className="inline-flex items-center gap-1.5">
                      <MapPin className="size-4" /> {posting.location ?? "-"}
                    </span>
                    <span>Closes {shortDate(posting.closesAt)}</span>
                  </div>

                  {posting.description && (
                    <p className="mt-4 line-clamp-3 text-sm text-muted-foreground">{posting.description}</p>
                  )}

                  <div className="mt-4 flex flex-wrap gap-2">
                    <EligibilityChips criteria={criteria} />
                  </div>

                  <div className="mt-auto flex flex-wrap justify-end gap-2 pt-5">
                    <Button
                      type="button"
                      variant="outline"
                      onClick={() => onCheckFit(posting)}
                      disabled={!me.data || fit.isPending}
                    >
                      {fit.isPending && fitFor?.id === posting.id ? (
                        <Loader2 className="size-4 animate-spin" />
                      ) : (
                        <Sparkles className="size-4" />
                      )}
                      Check fit
                    </Button>
                    <Button
                      type="button"
                      variant="accent"
                      onClick={() => apply.mutate(posting.id)}
                      disabled={!me.data || apply.isPending}
                    >
                      {apply.isPending && <Loader2 className="size-4 animate-spin" />}
                      Apply
                    </Button>
                  </div>

                  <span className="sr-only">Opportunity {index + 1}</span>
                </CardContent>
              </Card>
            );
          })}
        </div>
      </DataState>

      <Dialog
        open={!!fitFor}
        onClose={() => {
          setFitFor(null);
          setFitReport(null);
        }}
        title={fitFor ? `Fit check: ${fitFor.title}` : "Fit check"}
        description={fitReport ? (fitReport.aiLive ? "Live AI analysis" : "Offline analysis") : undefined}
      >
        {!fitReport && (
          <div className="flex items-center justify-center py-10 text-muted-foreground">
            <Loader2 className="size-6 animate-spin" />
          </div>
        )}
        {fitReport && (
          <div className="space-y-4">
            <div className="flex items-center gap-2">
              {fitReport.eligible ? (
                <>
                  <CheckCircle2 className="size-5 text-success" />
                  <StatusBadge status="ELIGIBLE" />
                </>
              ) : (
                <>
                  <Briefcase className="size-5 text-amber-500" />
                  <StatusBadge status="GAPS" />
                </>
              )}
            </div>
            {fitReport.eligibilityGaps.length > 0 && (
              <div className="rounded-lg border border-border bg-muted/30 p-4">
                <p className="mb-2 text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                  Eligibility gaps
                </p>
                <ul className="space-y-1.5 text-sm text-muted-foreground">
                  {fitReport.eligibilityGaps.map((gap) => (
                    <li key={gap} className="flex gap-2">
                      <span className="mt-2 size-1.5 shrink-0 rounded-full bg-accent" />
                      <span>{gap}</span>
                    </li>
                  ))}
                </ul>
              </div>
            )}
            <p className="whitespace-pre-wrap text-sm text-muted-foreground">{fitReport.explanation}</p>
          </div>
        )}
      </Dialog>
    </div>
  );
}

function EligibilityChips({ criteria }: { criteria: EligibilityCriteria }) {
  const chips = [
    criteria.minCgpa != null ? <Badge key="cgpa" tone="accent">CGPA &gt;= {criteria.minCgpa}</Badge> : null,
    criteria.maxBacklogs != null ? (
      <Badge key="backlogs" tone="accent">Backlogs &lt;= {criteria.maxBacklogs}</Badge>
    ) : null,
    criteria.batchYear != null ? <Badge key="batch" tone="accent">Batch {criteria.batchYear}</Badge> : null,
    ...(criteria.branches ?? []).map((branch) => (
      <Badge key={branch} tone="info">{branch}</Badge>
    )),
  ].filter(Boolean);

  return chips.length ? chips : <span className="text-xs text-muted-foreground">No eligibility constraints</span>;
}

function parseEligibility(json?: string | null): EligibilityCriteria {
  if (!json) return {};
  try {
    return JSON.parse(json) as EligibilityCriteria;
  } catch {
    return {};
  }
}
