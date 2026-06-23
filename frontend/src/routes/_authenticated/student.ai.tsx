import { useMemo, useState } from "react";
import { createFileRoute } from "@tanstack/react-router";
import { useMutation, useQuery } from "@tanstack/react-query";
import { Loader2, MessageSquareText, Sparkles } from "lucide-react";
import { toast } from "sonner";
import { jobFit, mockInterview, readiness, resumeFeedback } from "@/lib/api/ai";
import { ApiError } from "@/lib/api/client";
import { listPostings } from "@/lib/api/placement";
import { getMyProfile } from "@/lib/api/students";
import { DataState, PageHeader, TabBar } from "@/components/common";
import { ReadinessCard } from "@/components/student/ReadinessCard";
import { Badge, StatusBadge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Field } from "@/components/ui/field";
import { Input, Textarea } from "@/components/ui/input";
import { Select } from "@/components/ui/select";

export const Route = createFileRoute("/_authenticated/student/ai")({
  component: StudentAiPage,
});

type Tab = "readiness" | "resume" | "interview" | "jobFit";

function StudentAiPage() {
  const [tab, setTab] = useState<Tab>("readiness");
  const [resumeText, setResumeText] = useState("");
  const [role, setRole] = useState("");
  const [postingId, setPostingId] = useState("");

  const me = useQuery({ queryKey: ["myProfile"], queryFn: getMyProfile });
  const readinessQuery = useQuery({
    queryKey: ["readiness", me.data?.id],
    queryFn: () => readiness(me.data!.id),
    enabled: !!me.data?.id,
  });
  const postings = useQuery({ queryKey: ["postings", "all"], queryFn: () => listPostings(false) });

  const sourceLive = readinessQuery.data?.aiLive;
  const sourceBadge = sourceLive == null ? null : (
    <Badge tone={sourceLive ? "success" : "neutral"}>{sourceLive ? "LIVE AI" : "OFFLINE"}</Badge>
  );

  const feedback = useMutation({
    mutationFn: () => resumeFeedback(me.data!.id, resumeText.trim()),
    onError: (e) => toast.error(e instanceof ApiError ? e.detail : "Unable to generate feedback"),
  });

  const interview = useMutation({
    mutationFn: () => mockInterview(me.data!.id, role.trim() || undefined),
    onError: (e) => toast.error(e instanceof ApiError ? e.detail : "Unable to generate questions"),
  });

  const fit = useMutation({
    mutationFn: () => jobFit(me.data!.id, Number(postingId)),
    onError: (e) => toast.error(e instanceof ApiError ? e.detail : "Unable to check job fit"),
  });

  const selectedPosting = useMemo(
    () => postings.data?.find((posting) => posting.id === Number(postingId)),
    [postingId, postings.data],
  );

  return (
    <div>
      <PageHeader
        title="AI Career"
        description="Readiness, resume feedback, mock interview, and job-fit tools."
        actions={
          <TabBar<Tab>
            value={tab}
            onChange={setTab}
            layoutId="student-ai-tabs"
            tabs={[
              { value: "readiness", label: "Readiness" },
              { value: "resume", label: "Resume" },
              { value: "interview", label: "Mock interview" },
              { value: "jobFit", label: "Job-fit" },
            ]}
          />
        }
      />

      <DataState isLoading={me.isLoading} error={me.error}>
        {me.data && (
          <>
            {tab === "readiness" && (
              <DataState isLoading={readinessQuery.isLoading} error={readinessQuery.error}>
                {readinessQuery.data && <ReadinessCard report={readinessQuery.data} />}
              </DataState>
            )}

            {tab === "resume" && (
              <ToolLayout
                title="Resume feedback"
                sourceBadge={sourceBadge}
                control={
                  <form
                    className="space-y-4"
                    onSubmit={(e) => {
                      e.preventDefault();
                      feedback.mutate();
                    }}
                  >
                    <Field label="Resume text">
                      <Textarea
                        value={resumeText}
                        onChange={(e) => setResumeText(e.target.value)}
                        rows={10}
                        required
                        placeholder="Paste your resume content here."
                      />
                    </Field>
                    <div className="flex justify-end">
                      <Button type="submit" variant="accent" disabled={feedback.isPending || !resumeText.trim()}>
                        {feedback.isPending ? <Loader2 className="size-4 animate-spin" /> : <Sparkles className="size-4" />}
                        Generate
                      </Button>
                    </div>
                  </form>
                }
              >
                <OutputText
                  isLoading={feedback.isPending}
                  text={feedback.data?.feedback}
                  empty="Generated resume feedback will appear here."
                />
              </ToolLayout>
            )}

            {tab === "interview" && (
              <ToolLayout
                title="Mock interview"
                sourceBadge={sourceBadge}
                control={
                  <form
                    className="space-y-4"
                    onSubmit={(e) => {
                      e.preventDefault();
                      interview.mutate();
                    }}
                  >
                    <Field label="Target role" hint="Optional">
                      <Input
                        value={role}
                        onChange={(e) => setRole(e.target.value)}
                        placeholder="Software engineer, data analyst, product intern"
                      />
                    </Field>
                    <div className="flex justify-end">
                      <Button type="submit" variant="accent" disabled={interview.isPending}>
                        {interview.isPending ? <Loader2 className="size-4 animate-spin" /> : <MessageSquareText className="size-4" />}
                        Generate
                      </Button>
                    </div>
                  </form>
                }
              >
                <OutputText
                  isLoading={interview.isPending}
                  text={interview.data?.questions}
                  empty="Generated interview questions will appear here."
                />
              </ToolLayout>
            )}

            {tab === "jobFit" && (
              <ToolLayout
                title="Job-fit"
                sourceBadge={fit.data ? <Badge tone={fit.data.aiLive ? "success" : "neutral"}>{fit.data.aiLive ? "LIVE AI" : "OFFLINE"}</Badge> : sourceBadge}
                control={
                  <form
                    className="space-y-4"
                    onSubmit={(e) => {
                      e.preventDefault();
                      fit.mutate();
                    }}
                  >
                    <Field label="Posting">
                      <Select
                        value={postingId}
                        onChange={(e) => setPostingId(e.target.value)}
                        required
                        disabled={postings.isLoading}
                      >
                        <option value="">Select a posting</option>
                        {(postings.data ?? []).map((posting) => (
                          <option key={posting.id} value={posting.id}>
                            {posting.title}
                          </option>
                        ))}
                      </Select>
                    </Field>
                    <div className="flex justify-end">
                      <Button type="submit" variant="accent" disabled={fit.isPending || !postingId}>
                        {fit.isPending ? <Loader2 className="size-4 animate-spin" /> : <Sparkles className="size-4" />}
                        Check fit
                      </Button>
                    </div>
                  </form>
                }
              >
                {fit.data ? (
                  <div className="space-y-4">
                    <div className="flex flex-wrap items-center gap-2">
                      <StatusBadge status={fit.data.eligible ? "ELIGIBLE" : "GAPS"} />
                      {selectedPosting && <span className="text-sm text-muted-foreground">{selectedPosting.title}</span>}
                    </div>
                    {fit.data.eligibilityGaps.length > 0 && (
                      <div className="rounded-lg border border-border bg-muted/30 p-4">
                        <p className="mb-2 text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                          Eligibility gaps
                        </p>
                        <ul className="space-y-1.5 text-sm text-muted-foreground">
                          {fit.data.eligibilityGaps.map((gap) => (
                            <li key={gap} className="flex gap-2">
                              <span className="mt-2 size-1.5 shrink-0 rounded-full bg-accent" />
                              <span>{gap}</span>
                            </li>
                          ))}
                        </ul>
                      </div>
                    )}
                    <p className="whitespace-pre-wrap text-sm text-muted-foreground">{fit.data.explanation}</p>
                  </div>
                ) : (
                  <OutputText
                    isLoading={fit.isPending}
                    empty={postings.isLoading ? "Loading postings." : "Job-fit analysis will appear here."}
                  />
                )}
              </ToolLayout>
            )}
          </>
        )}
      </DataState>
    </div>
  );
}

function ToolLayout({
  title,
  sourceBadge,
  control,
  children,
}: {
  title: string;
  sourceBadge: React.ReactNode;
  control: React.ReactNode;
  children: React.ReactNode;
}) {
  return (
    <div className="grid gap-6 lg:grid-cols-[0.85fr_1.15fr]">
      <Card>
        <CardContent className="p-6">
          <div className="mb-5 flex items-center justify-between gap-3">
            <h2 className="font-semibold">{title}</h2>
            {sourceBadge}
          </div>
          {control}
        </CardContent>
      </Card>
      <Card>
        <CardContent className="p-6">{children}</CardContent>
      </Card>
    </div>
  );
}

function OutputText({
  isLoading,
  text,
  empty,
}: {
  isLoading: boolean;
  text?: string;
  empty: string;
}) {
  if (isLoading) {
    return (
      <div className="flex items-center justify-center py-16 text-muted-foreground">
        <Loader2 className="size-6 animate-spin" />
      </div>
    );
  }
  if (!text) {
    return <p className="py-8 text-sm text-muted-foreground">{empty}</p>;
  }
  return <p className="whitespace-pre-wrap text-sm text-muted-foreground">{text}</p>;
}
