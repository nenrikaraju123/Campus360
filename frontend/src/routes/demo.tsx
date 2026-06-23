import { createFileRoute, Link } from "@tanstack/react-router";
import {
  ArrowRight,
  BarChart3,
  BriefcaseBusiness,
  Building2,
  CheckCircle2,
  ClipboardCheck,
  GraduationCap,
  Layers3,
  PlayCircle,
  Sparkles,
  UsersRound,
} from "lucide-react";
import { PublicNav } from "@/components/layout/PublicNav";
import { FadeUp } from "@/components/motion/FadeUp";
import { Button } from "@/components/ui/button";

export const Route = createFileRoute("/demo")({
  component: DemoPage,
});

const MODULES = [
  {
    icon: Building2,
    title: "Platform administration",
    body: "Review institution registrations, onboard campuses, and manage institution lifecycle from a central console.",
  },
  {
    icon: Layers3,
    title: "Academic structure",
    body: "Organize departments, programs, courses, terms, and sections so every campus has a clear foundation.",
  },
  {
    icon: UsersRound,
    title: "Student management",
    body: "Maintain student profiles, academic progress, branches, batches, CGPA, and placement readiness details.",
  },
  {
    icon: BriefcaseBusiness,
    title: "Placement operations",
    body: "Manage companies, publish opportunities, track applications, shortlist students, and issue offers.",
  },
  {
    icon: Sparkles,
    title: "Career support",
    body: "Provide readiness scoring, resume feedback, mock interview preparation, and job-fit guidance.",
  },
  {
    icon: BarChart3,
    title: "Leadership visibility",
    body: "Give management a clear view of student progress, placement activity, offers, and institutional outcomes.",
  },
];

const WALKTHROUGH = [
  "Start with the public landing page and explain the institution onboarding request.",
  "Open the platform console to show registration review and institution management.",
  "Move to the institution workspace and show academic structure and student records.",
  "Open placement operations to show companies, job postings, eligibility, applications, and offers.",
  "Finish with the student portal: opportunities, applications, offers, and career guidance.",
];

const AUDIENCE_POINTS = [
  "For administrators: less manual coordination and cleaner daily operations.",
  "For placement teams: one place to manage recruiters, opportunities, applications, and offers.",
  "For students: a focused portal for career readiness, job discovery, and offer responses.",
  "For leadership: reliable visibility into campus and placement performance.",
];

function DemoPage() {
  return (
    <div className="min-h-screen overflow-x-hidden bg-background">
      <PublicNav />

      <section className="relative isolate overflow-hidden border-b border-border bg-muted/35">
        <div className="absolute inset-0 -z-10 bg-grid opacity-70" />
        <div className="absolute inset-0 -z-10 bg-background/70" />
        <div className="mx-auto grid max-w-6xl gap-12 px-6 pb-16 pt-10 md:pb-20 md:pt-14 lg:grid-cols-[minmax(0,1fr)_420px] lg:items-start lg:pb-24 lg:pt-16">
          <div className="max-w-3xl">
            <span className="inline-flex items-center gap-2 rounded-full border border-border bg-background/90 px-3 py-1 text-xs font-semibold uppercase tracking-wide text-accent shadow-sm">
              <span className="size-1.5 rounded-full bg-accent" />
              Project demo overview
            </span>
            <h1 className="mt-4 max-w-3xl text-3xl font-semibold leading-[1.08] tracking-tight sm:text-4xl md:text-6xl lg:text-5xl">
              Campus360 is a complete digital workspace for modern institutions.
            </h1>
            <p className="mt-6 max-w-2xl text-base leading-7 text-muted-foreground sm:text-lg sm:leading-8">
              This demo shows how colleges and universities can manage onboarding, academics,
              students, placements, career readiness, and leadership reporting from one connected
              platform.
            </p>
            <div className="mt-8 flex flex-col gap-3 sm:flex-row">
              <Link to="/register" className="w-full sm:w-auto">
                <Button variant="accent" size="lg" className="w-full sm:w-auto">
                  Start demo flow
                  <ArrowRight className="size-4" />
                </Button>
              </Link>
              <Link to="/login" className="w-full sm:w-auto">
                <Button variant="outline" size="lg" className="w-full sm:w-auto">
                  Open login
                </Button>
              </Link>
            </div>
          </div>

          <div className="rounded-lg border border-border bg-card/95 p-5 shadow-2xl">
            <div className="flex items-center gap-3 border-b border-border pb-4">
              <div className="grid size-10 place-items-center rounded-md bg-accent text-accent-foreground">
                <PlayCircle className="size-5" />
              </div>
              <div>
                <p className="text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                  Demo story
                </p>
                <h2 className="font-semibold">From campus request to placement outcome</h2>
              </div>
            </div>
            <div className="mt-5 grid gap-3">
              {["Onboard institution", "Build academic structure", "Manage students", "Run placements", "Support careers"].map(
                (item, index) => (
                  <div key={item} className="flex items-center gap-3 rounded-md border border-border bg-background p-3">
                    <span className="grid size-7 shrink-0 place-items-center rounded-md bg-accent/10 font-mono text-xs text-accent">
                      {index + 1}
                    </span>
                    <span className="text-sm font-medium">{item}</span>
                  </div>
                ),
              )}
            </div>
          </div>
        </div>
      </section>

      <section className="border-b border-border bg-card">
        <div className="mx-auto grid max-w-6xl gap-px bg-border sm:grid-cols-2 lg:grid-cols-4">
          {[
            ["3", "Role areas"],
            ["6", "Core modules"],
            ["End-to-end", "Placement workflow"],
            ["Static", "Shareable demo page"],
          ].map(([value, label]) => (
            <div key={label} className="bg-card px-6 py-6">
              <div className="text-lg font-semibold">{value}</div>
              <div className="mt-1 text-sm text-muted-foreground">{label}</div>
            </div>
          ))}
        </div>
      </section>

      <section className="border-b border-border bg-muted/30">
        <div className="mx-auto max-w-6xl px-6 py-20 md:py-24">
          <FadeUp>
            <p className="text-xs font-semibold uppercase tracking-widest text-accent">What The Project Covers</p>
            <h2 className="mt-3 max-w-3xl text-3xl font-semibold tracking-tight md:text-4xl">
              A complete campus operations and placement management experience.
            </h2>
          </FadeUp>
          <div className="mt-12 grid gap-px overflow-hidden rounded-lg border border-border bg-border sm:grid-cols-2 lg:grid-cols-3">
            {MODULES.map((module) => (
              <div key={module.title} className="bg-card p-7">
                <div className="grid size-10 place-items-center rounded-md bg-accent/10 text-accent">
                  <module.icon className="size-5" />
                </div>
                <h3 className="mt-5 font-semibold">{module.title}</h3>
                <p className="mt-2 text-sm leading-6 text-muted-foreground">{module.body}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      <section className="border-b border-border">
        <div className="mx-auto grid max-w-6xl gap-10 px-6 py-20 md:grid-cols-[0.9fr_1.1fr] md:py-24">
          <FadeUp>
            <p className="text-xs font-semibold uppercase tracking-widest text-accent">Demo Walkthrough</p>
            <h2 className="mt-3 text-3xl font-semibold tracking-tight md:text-4xl">
              Use this page as your project presentation guide.
            </h2>
            <p className="mt-4 text-base leading-7 text-muted-foreground">
              Share this route with others to explain what the project does before opening the
              actual app screens.
            </p>
          </FadeUp>

          <div className="grid gap-3">
            {WALKTHROUGH.map((step, index) => (
              <FadeUp key={step} delay={index * 0.05}>
                <div className="flex gap-4 rounded-lg border border-border bg-card p-5">
                  <span className="grid size-9 shrink-0 place-items-center rounded-md bg-accent/10 font-mono text-xs text-accent">
                    {String(index + 1).padStart(2, "0")}
                  </span>
                  <p className="text-sm leading-6 text-muted-foreground">{step}</p>
                </div>
              </FadeUp>
            ))}
          </div>
        </div>
      </section>

      <section className="border-b border-border bg-card">
        <div className="mx-auto grid max-w-6xl gap-10 px-6 py-20 md:grid-cols-[1fr_1fr] md:py-24">
          <FadeUp>
            <p className="text-xs font-semibold uppercase tracking-widest text-accent">Why It Matters</p>
            <h2 className="mt-3 text-3xl font-semibold tracking-tight md:text-4xl">
              Designed for real institution workflows.
            </h2>
          </FadeUp>
          <div className="grid gap-3">
            {AUDIENCE_POINTS.map((point) => (
              <div key={point} className="flex items-start gap-3 text-sm leading-6">
                <CheckCircle2 className="mt-0.5 size-5 shrink-0 text-success" />
                <span>{point}</span>
              </div>
            ))}
          </div>
        </div>
      </section>

      <section>
        <div className="mx-auto max-w-6xl px-6 py-20">
          <FadeUp className="rounded-lg border border-border bg-foreground px-6 py-12 text-background md:px-12">
            <div className="grid gap-8 md:grid-cols-[1fr_auto] md:items-center">
              <div>
                <h2 className="text-3xl font-semibold tracking-tight md:text-4xl">
                  Ready to show the project?
                </h2>
                <p className="mt-4 max-w-2xl text-base leading-7 text-background/75">
                  Start with this demo overview, then open the app login to walk through platform,
                  institution, and student experiences.
                </p>
              </div>
              <Link to="/login">
                <Button variant="accent" size="lg">
                  Open app
                  <ArrowRight className="size-4" />
                </Button>
              </Link>
            </div>
          </FadeUp>
        </div>
      </section>
    </div>
  );
}
