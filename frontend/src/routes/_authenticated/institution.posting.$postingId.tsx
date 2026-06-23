import { useState } from "react";
import { createFileRoute, Link } from "@tanstack/react-router";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { ArrowLeft, Loader2, Award, Users2 } from "lucide-react";
import { toast } from "sonner";
import {
  getPosting, eligibleStudents, applicationsForPosting, listCompanies,
  makeOffer, updateApplicationStatus, getCareerProfile
} from "@/lib/api/placement";
import { listStudents } from "@/lib/api/students";
import type { Application, EligibilityCriteria } from "@/lib/api/entities";
import { ApiError } from "@/lib/api/client";
import { TabBar, DataState, moneyINR } from "@/components/common";
import { Badge, StatusBadge } from "@/components/ui/badge";
import { DataTable, Row, Cell } from "@/components/ui/table";
import { Card, CardContent } from "@/components/ui/card";
import { Dialog } from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Field } from "@/components/ui/field";

export const Route = createFileRoute("/_authenticated/institution/posting/$postingId")({
  component: PostingDetail,
});

const errToast = (e: unknown) => toast.error(e instanceof ApiError ? e.detail : "Action failed");

function PostingDetail() {
  const { postingId } = Route.useParams();
  const id = Number(postingId);
  const qc = useQueryClient();
  const [tab, setTab] = useState<"eligible" | "applications">("eligible");
  const [offerFor, setOfferFor] = useState<Application | null>(null);
  const [viewingProfile, setViewingProfile] = useState<number | null>(null);

  const posting = useQuery({ queryKey: ["posting", id], queryFn: () => getPosting(id) });
  const companies = useQuery({ queryKey: ["companies"], queryFn: listCompanies });
  const students = useQuery({ queryKey: ["students"], queryFn: listStudents });
  const eligible = useQuery({ queryKey: ["eligible", id], queryFn: () => eligibleStudents(id), enabled: tab === "eligible" });
  const apps = useQuery({ queryKey: ["applications", id], queryFn: () => applicationsForPosting(id), enabled: tab === "applications" });


  const offer = useMutation({
    mutationFn: ({ applicationId, body }: { applicationId: number; body: { ctc?: number; joiningDate?: string } }) =>
      makeOffer(applicationId, body),
    onSuccess: () => { setOfferFor(null); qc.invalidateQueries({ queryKey: ["applications", id] }); qc.invalidateQueries({ queryKey: ["placementStats"] }); toast.success("Offer extended"); },
    onError: errToast,
  });

  const status = useMutation({
    mutationFn: ({ applicationId, value }: { applicationId: number; value: string }) =>
      updateApplicationStatus(applicationId, value),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ["applications", id] }); toast.success("Status updated"); },
    onError: errToast,
  });

  const p = posting.data;
  const companyName = (cid: number) => companies.data?.find((c) => c.id === cid)?.name ?? `#${cid}`;
  const roll = (sid: number) => students.data?.find((s) => s.id === sid)?.rollNumber ?? `#${sid}`;
  const criteria: EligibilityCriteria = p?.eligibility ? safeParse(p.eligibility) : {};

  return (
    <div className="mx-auto max-w-5xl">
      <Link to="/institution/placement" className="mb-5 inline-flex items-center gap-1.5 text-sm text-muted-foreground hover:text-foreground">
        <ArrowLeft className="size-4" /> Back to placements
      </Link>

      <DataState isLoading={posting.isLoading} error={posting.error}>
        {p && (
          <>
            <Card className="mb-6">
              <CardContent className="p-6">
                <div className="flex flex-wrap items-start justify-between gap-4">
                  <div>
                    <div className="flex items-center gap-3">
                      <h1 className="text-2xl font-semibold tracking-tight">{p.title}</h1>
                      <StatusBadge status={p.status} />
                    </div>
                    <p className="mt-1 text-muted-foreground">
                      {companyName(p.companyId)} · {p.type} · {p.location ?? "—"}
                    </p>
                  </div>
                  <div className="text-right">
                    <div className="font-mono text-2xl font-semibold">{moneyINR(p.ctc)}</div>
                    <div className="text-xs text-muted-foreground">per year</div>
                  </div>
                </div>
                <div className="mt-4 flex flex-wrap gap-2">
                  {criteria.minCgpa != null && <Badge tone="accent">CGPA ≥ {criteria.minCgpa}</Badge>}
                  {criteria.maxBacklogs != null && <Badge tone="accent">≤ {criteria.maxBacklogs} backlogs</Badge>}
                  {criteria.batchYear != null && <Badge tone="accent">Batch {criteria.batchYear}</Badge>}
                  {criteria.branches?.map((b) => <Badge key={b} tone="info">{b}</Badge>)}
                  {!criteria.minCgpa && !criteria.branches?.length && !criteria.maxBacklogs && !criteria.batchYear && (
                    <span className="text-xs text-muted-foreground">No eligibility constraints</span>
                  )}
                </div>
              </CardContent>
            </Card>

            <div className="mb-4">
              <TabBar
                value={tab}
                onChange={setTab}
                tabs={[
                  { value: "eligible", label: "Eligible students" },
                  { value: "applications", label: "Applications" },
                ]}
              />
            </div>

            {tab === "eligible" && (
              <DataState isLoading={eligible.isLoading} error={eligible.error} isEmpty={(eligible.data ?? []).length === 0} emptyTitle="No eligible students" emptyBody="No students currently meet the eligibility rules.">
                <DataTable columns={["Roll no.", "Branch", "CGPA", "Backlogs", "Batch"]}>
                  {(eligible.data ?? []).map((s, i) => (
                    <Row key={s.id} index={i}>
                      <Cell className="font-mono text-xs uppercase">{s.rollNumber}</Cell>
                      <Cell>{s.branch ?? "—"}</Cell>
                      <Cell className="font-mono font-medium">{Number(s.cgpa).toFixed(2)}</Cell>
                      <Cell className="font-mono">{s.activeBacklogs}</Cell>
                      <Cell className="font-mono">{s.batchYear ?? "—"}</Cell>
                    </Row>
                  ))}
                </DataTable>
              </DataState>
            )}

            {tab === "applications" && (
              <DataState isLoading={apps.isLoading} error={apps.error} isEmpty={(apps.data ?? []).length === 0} emptyTitle="No applications yet" emptyBody="Applications from students will appear here.">
                <DataTable columns={["Roll no.", "Status", "Applied", ""]}>
                  {(apps.data ?? []).map((a, i) => (
                    <Row key={a.id} index={i}>
                      <Cell className="font-mono text-xs uppercase">{roll(a.studentId)}</Cell>
                      <Cell><StatusBadge status={a.status} /></Cell>
                      <Cell className="whitespace-nowrap text-xs text-muted-foreground">
                        {new Date(a.appliedAt).toLocaleDateString("en-IN")}
                      </Cell>
                      <Cell>
                        <div className="flex justify-end gap-2">
                          <Button variant="outline" size="sm" onClick={() => setViewingProfile(a.studentId)}>
                            <Award className="size-3.5" /> Profile
                          </Button>
                          {a.status === "APPLIED" && (
                            <Button variant="outline" size="sm" disabled={status.isPending} onClick={() => status.mutate({ applicationId: a.id, value: "SHORTLISTED" })}>
                              <Users2 className="size-3.5" /> Shortlist
                            </Button>
                          )}
                          {a.status !== "OFFERED" && (
                            <Button variant="accent" size="sm" onClick={() => setOfferFor(a)}>
                              <Award className="size-3.5" /> Offer
                            </Button>
                          )}
                        </div>
                      </Cell>
                    </Row>
                  ))}
                </DataTable>
              </DataState>
            )}
          </>
        )}
      </DataState>

      {/* Make offer */}
      <Dialog open={!!offerFor} onClose={() => setOfferFor(null)} title="Extend an offer" description={offerFor ? roll(offerFor.studentId) : undefined}>
        {offerFor && (
          <form
            onSubmit={(e) => {
              e.preventDefault();
              const f = new FormData(e.currentTarget);
              offer.mutate({
                applicationId: offerFor.id,
                body: {
                  ctc: f.get("ctc") ? Number(f.get("ctc")) : undefined,
                  joiningDate: (f.get("joiningDate") as string) || undefined,
                },
              });
            }}
            className="space-y-4"
          >
            <div className="grid gap-4 sm:grid-cols-2">
              <Field label="CTC (₹/yr)"><Input name="ctc" type="number" defaultValue={p?.ctc ?? undefined} /></Field>
              <Field label="Joining date"><Input name="joiningDate" type="date" /></Field>
            </div>
            <div className="flex justify-end gap-2">
              <Button type="button" variant="ghost" onClick={() => setOfferFor(null)}>Cancel</Button>
              <Button type="submit" variant="accent" disabled={offer.isPending}>
                {offer.isPending && <Loader2 className="size-4 animate-spin" />}
                Extend offer
              </Button>
            </div>
          </form>
        )}
      </Dialog>

      {/* View Profile */}
      {viewingProfile && (
        <ProfileViewDialog studentId={viewingProfile} onClose={() => setViewingProfile(null)} />
      )}
    </div>
  );
}

function ProfileViewDialog({ studentId, onClose }: { studentId: number; onClose: () => void }) {
  const profileQuery = useQuery({
    queryKey: ["careerProfile", studentId],
    queryFn: () => getCareerProfile(studentId),
  });

  return (
    <Dialog open={true} onClose={onClose} title="Career Profile">
      {profileQuery.isLoading ? (
        <div className="py-8 text-center text-muted-foreground">Loading profile...</div>
      ) : !profileQuery.data ? (
        <div className="py-8 text-center text-muted-foreground">This student has not set up their career profile yet.</div>
      ) : (
        <div className="space-y-4">
          <div className="grid gap-4 sm:grid-cols-2">
            <div>
              <p className="text-xs font-semibold text-muted-foreground uppercase">Readiness Score</p>
              <p className="font-semibold text-lg">{profileQuery.data.readinessScore}/100</p>
            </div>
            <div>
              <p className="text-xs font-semibold text-muted-foreground uppercase">Resume Link</p>
              {profileQuery.data.resumeRef ? (
                <a href={profileQuery.data.resumeRef} target="_blank" rel="noreferrer" className="text-sm text-accent hover:underline">
                  View Resume
                </a>
              ) : (
                <p className="text-sm text-muted-foreground">None provided</p>
              )}
            </div>
          </div>
          <div>
            <p className="text-xs font-semibold text-muted-foreground uppercase mb-1">Key Skills</p>
            <p className="text-sm whitespace-pre-wrap">{profileQuery.data.skills || "None listed"}</p>
          </div>
          <div>
            <p className="text-xs font-semibold text-muted-foreground uppercase mb-1">Certifications & Achievements</p>
            <p className="text-sm whitespace-pre-wrap">{profileQuery.data.certifications || "None listed"}</p>
          </div>
          <div>
            <p className="text-xs font-semibold text-muted-foreground uppercase mb-1">Notable Projects</p>
            <p className="text-sm whitespace-pre-wrap">{profileQuery.data.projects || "None listed"}</p>
          </div>
        </div>
      )}
    </Dialog>
  );
}

function safeParse(json: string): EligibilityCriteria {
  try {
    return JSON.parse(json) as EligibilityCriteria;
  } catch {
    return {};
  }
}
