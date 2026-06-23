import { createFileRoute, Link } from "@tanstack/react-router";
import { useQuery } from "@tanstack/react-query";
import { ArrowRight, Briefcase, FileStack, GraduationCap, Sparkles } from "lucide-react";
import { readiness } from "@/lib/api/ai";
import { getMyProfile } from "@/lib/api/students";
import { useAuthStore } from "@/lib/auth/store";
import { ReadinessCard } from "@/components/student/ReadinessCard";
import { DataState, PageHeader, StatCard } from "@/components/common";
import { Card, CardContent } from "@/components/ui/card";

export const Route = createFileRoute("/_authenticated/student/")({
  component: StudentDashboard,
});

function StudentDashboard() {
  const user = useAuthStore((s) => s.user);
  const me = useQuery({ queryKey: ["myProfile"], queryFn: getMyProfile });
  const readinessQuery = useQuery({
    queryKey: ["readiness", me.data?.id],
    queryFn: () => readiness(me.data!.id),
    enabled: !!me.data?.id,
  });

  const profile = me.data;
  const quickLinks = [
    {
      to: "/student/opportunities",
      label: "Opportunities",
      body: "Browse open roles and check your eligibility before applying.",
      icon: Briefcase,
    },
    {
      to: "/student/applications",
      label: "Applications",
      body: "Track application status and respond to placement offers.",
      icon: FileStack,
    },
    {
      to: "/student/ai",
      label: "AI Career",
      body: "Use readiness, resume feedback, mock interview, and job-fit tools.",
      icon: Sparkles,
    },
  ] as const;

  return (
    <div>
      <PageHeader
        title="Student overview"
        description="Your placement profile, readiness, and career actions."
      />

      <DataState isLoading={me.isLoading} error={me.error}>
        {profile && (
          <>
            <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
              <StatCard label="Roll number" value={profile.rollNumber} icon={GraduationCap} index={0} />
              <StatCard label="Branch" value={profile.branch ?? "-"} icon={GraduationCap} index={1} />
              <StatCard label="CGPA" value={Number(profile.cgpa).toFixed(2)} icon={GraduationCap} index={2} />
              <StatCard label="Backlogs" value={profile.activeBacklogs} icon={GraduationCap} index={3} />
            </div>

            <div className="mt-8 grid gap-6 lg:grid-cols-[1.2fr_0.8fr]">
              <DataState isLoading={readinessQuery.isLoading} error={readinessQuery.error}>
                {readinessQuery.data && <ReadinessCard report={readinessQuery.data} />}
              </DataState>

              <Card>
                <CardContent className="p-6">
                  <h2 className="font-semibold">Identity</h2>
                  <dl className="mt-4 space-y-3 text-sm">
                    <div className="flex justify-between gap-4">
                      <dt className="text-muted-foreground">Email</dt>
                      <dd className="truncate font-mono text-xs">{user?.email ?? "-"}</dd>
                    </div>
                    <div className="flex justify-between gap-4">
                      <dt className="text-muted-foreground">Batch</dt>
                      <dd className="font-mono">{profile.batchYear ?? "-"}</dd>
                    </div>
                    <div className="flex justify-between gap-4">
                      <dt className="text-muted-foreground">Current term</dt>
                      <dd className="font-mono">{profile.currentTerm}</dd>
                    </div>
                    <div className="flex justify-between gap-4">
                      <dt className="text-muted-foreground">Student ID</dt>
                      <dd className="font-mono">#{profile.id}</dd>
                    </div>
                  </dl>
                </CardContent>
              </Card>
            </div>

            <div className="mt-8 grid gap-4 md:grid-cols-3">
              {quickLinks.map((link) => (
                <Link key={link.to} to={link.to}>
                  <Card className="group h-full transition-colors hover:border-accent/40">
                    <CardContent className="flex h-full flex-col p-6">
                      <div className="grid size-10 place-items-center rounded-lg bg-accent/10 text-accent">
                        <link.icon className="size-5" />
                      </div>
                      <h3 className="mt-4 font-semibold">{link.label}</h3>
                      <p className="mt-1 flex-1 text-sm text-muted-foreground">{link.body}</p>
                      <span className="mt-4 inline-flex items-center gap-2 text-sm font-medium text-accent">
                        Open <ArrowRight className="size-4 transition-transform group-hover:translate-x-0.5" />
                      </span>
                    </CardContent>
                  </Card>
                </Link>
              ))}
            </div>
          </>
        )}
      </DataState>
    </div>
  );
}
