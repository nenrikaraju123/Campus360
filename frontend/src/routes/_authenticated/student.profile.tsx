import { createFileRoute } from "@tanstack/react-router";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { getCareerProfile, updateCareerProfile } from "@/lib/api/placement";
import { useAuthStore } from "@/lib/auth/store";
import { PageHeader } from "@/components/common";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Field } from "@/components/ui/field";
import { toast } from "sonner";
import { FadeUp } from "@/components/motion/FadeUp";
import { BriefcaseBusiness, CheckCircle2 } from "lucide-react";

export const Route = createFileRoute("/_authenticated/student/profile")({
  component: StudentProfilePage,
});

function StudentProfilePage() {
  const queryClient = useQueryClient();
  const user = useAuthStore((s) => s.user);
  const studentId = user?.userId ?? 0;

  const { data: profile, isLoading } = useQuery({
    queryKey: ["careerProfile", studentId],
    queryFn: () => getCareerProfile(studentId),
    enabled: !!studentId,
  });

  const updateMutation = useMutation({
    mutationFn: (body: any) => updateCareerProfile(studentId, body),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["careerProfile", studentId] });
      toast.success("Career profile updated successfully");
    },
    onError: (err: any) => toast.error(err.detail || "Update failed"),
  });

  if (isLoading) return <div>Loading profile...</div>;

  return (
    <div className="space-y-6 max-w-3xl">
      <PageHeader
        title="Career Profile"
        description="Manage your resume, skills, and certifications for recruiters to see."
      />

      <FadeUp>
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <BriefcaseBusiness className="size-5 text-accent" />
              Professional Details
            </CardTitle>
          </CardHeader>
          <CardContent>
            <form
              className="space-y-6"
              onSubmit={(e) => {
                e.preventDefault();
                const fd = new FormData(e.currentTarget);
                updateMutation.mutate({
                  resumeRef: fd.get("resumeRef") as string,
                  skills: fd.get("skills") as string,
                  certifications: fd.get("certifications") as string,
                  projects: fd.get("projects") as string,
                });
              }}
            >
              <Field label="Resume Link (URL)">
                <Input name="resumeRef" defaultValue={profile?.resumeRef || ""} placeholder="https://drive.google.com/..." />
              </Field>

              <Field label="Key Skills (comma separated)">
                <textarea
                  name="skills"
                  defaultValue={profile?.skills || ""}
                  rows={2}
                  className="w-full rounded-md border border-input bg-transparent px-3 py-2 text-sm placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring"
                  placeholder="Java, Spring Boot, React, TypeScript..."
                />
              </Field>

              <Field label="Certifications & Achievements">
                <textarea
                  name="certifications"
                  defaultValue={profile?.certifications || ""}
                  rows={3}
                  className="w-full rounded-md border border-input bg-transparent px-3 py-2 text-sm placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring"
                  placeholder="AWS Certified Developer, LeetCode Top 5%..."
                />
              </Field>

              <Field label="Notable Projects">
                <textarea
                  name="projects"
                  defaultValue={profile?.projects || ""}
                  rows={4}
                  className="w-full rounded-md border border-input bg-transparent px-3 py-2 text-sm placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring"
                  placeholder="Built a scalable E-Commerce backend..."
                />
              </Field>

              <div className="flex justify-end gap-3 pt-4 border-t border-border">
                {profile && (
                  <div className="mr-auto flex items-center gap-2 text-sm text-muted-foreground">
                    <CheckCircle2 className="size-4 text-success" />
                    Readiness Score: <span className="font-bold text-foreground">{profile.readinessScore}/100</span>
                  </div>
                )}
                <Button type="submit" variant="accent" disabled={updateMutation.isPending}>
                  {updateMutation.isPending ? "Saving..." : "Save Profile"}
                </Button>
              </div>
            </form>
          </CardContent>
        </Card>
      </FadeUp>
    </div>
  );
}
