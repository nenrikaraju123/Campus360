import { createFileRoute, Link } from "@tanstack/react-router";
import {
  ArrowRight,
  BarChart3,
  BriefcaseBusiness,
  Building2,
  CalendarCheck2,
  CheckCircle2,
  ClipboardCheck,
  GraduationCap,
  LineChart,
  MessageSquareText,
  ShieldCheck,
  Sparkles,
  UsersRound,
} from "lucide-react";
import { PublicNav } from "@/components/layout/PublicNav";
import { FadeUp } from "@/components/motion/FadeUp";
import { Stagger, StaggerItem } from "@/components/motion/Stagger";
import { Button } from "@/components/ui/button";

export const Route = createFileRoute("/")({
  component: Landing,
});

const SERVICES = [
  {
    icon: Building2,
    title: "Institution onboarding",
    body: "Structured setup for colleges, universities, departments, users, and administrative teams.",
  },
  {
    icon: GraduationCap,
    title: "Student lifecycle management",
    body: "Centralized student records, academic progress, program details, and profile readiness.",
  },
  {
    icon: BriefcaseBusiness,
    title: "Placement operations",
    body: "Company management, opportunity publishing, student applications, shortlisting, and offers.",
  },
  {
    icon: Sparkles,
    title: "Career readiness support",
    body: "Resume feedback, interview preparation, job-fit guidance, and improvement plans for students.",
  },
  {
    icon: BarChart3,
    title: "Leadership reporting",
    body: "Clear dashboards for placement performance, active opportunities, student progress, and outcomes.",
  },
  {
    icon: ShieldCheck,
    title: "Enterprise governance",
    body: "Focused team workspaces, clean approvals, accountable actions, and controlled institutional access.",
  },
];

const AUDIENCES = [
  {
    icon: UsersRound,
    title: "For management",
    body: "A reliable view of campus operations, institutional performance, and placement results.",
  },
  {
    icon: ClipboardCheck,
    title: "For administrators",
    body: "Less manual coordination across departments, students, companies, and placement activities.",
  },
  {
    icon: GraduationCap,
    title: "For students",
    body: "A single portal to track readiness, discover opportunities, apply, and respond to offers.",
  },
];

const OUTCOMES = [
  "Reduce repeated manual work across academics and placements",
  "Improve transparency between institution teams and students",
  "Track placement activity from posting to final offer",
  "Give leadership clear, current information for decisions",
  "Provide students with guided career preparation",
];

const STEPS = [
  {
    n: "01",
    title: "Request onboarding",
    body: "Submit institution details and the primary administrator contact.",
  },
  {
    n: "02",
    title: "Configure workspace",
    body: "Set up departments, users, students, academic structure, and placement teams.",
  },
  {
    n: "03",
    title: "Operate with clarity",
    body: "Run daily academic and placement workflows from one organized system.",
  },
];

function Landing() {
  return (
    <div className="min-h-screen overflow-x-hidden bg-background">
      <PublicNav />

      <section className="relative isolate overflow-hidden border-b border-border bg-muted/35">
        <div className="absolute inset-0 -z-10 bg-grid opacity-70" />
        <div className="absolute inset-0 -z-10 bg-background/70" />
        <div className="relative z-10 mx-auto grid max-w-6xl gap-12 px-6 pb-14 pt-10 sm:pb-16 sm:pt-12 md:pb-20 md:pt-14 lg:grid-cols-[minmax(0,1fr)_minmax(360px,440px)] lg:items-start lg:pb-24 lg:pt-16">
          <div className="min-w-0 max-w-2xl">
            <span className="inline-flex max-w-[22rem] items-center gap-2 rounded-full border border-border bg-background/90 px-3 py-1 text-xs font-semibold uppercase tracking-wide text-accent shadow-sm sm:max-w-none">
              <span className="size-1.5 rounded-full bg-accent" />
              Complete campus transformation platform
            </span>
            <h1 className="mt-4 max-w-[22rem] text-3xl font-semibold leading-[1.08] tracking-tight sm:max-w-none sm:text-4xl md:text-6xl lg:text-5xl xl:text-6xl">
              Transform campus operations from{" "}
              <span className="text-accent">admissions to placements</span>.
            </h1>
            <p className="mt-6 max-w-[22rem] text-base leading-7 text-muted-foreground sm:max-w-2xl sm:text-lg sm:leading-8">
              Campus360 helps colleges and universities replace scattered manual work with one
              enterprise-ready workspace for managing institutional operations, student progress,
              recruiter coordination, career preparation, and placement outcomes.
            </p>
            <div className="mt-8 flex max-w-[22rem] flex-col gap-3 sm:max-w-none sm:flex-row sm:flex-wrap sm:items-center">
              <Link to="/register" className="w-full sm:w-auto">
                <Button variant="accent" size="lg" className="w-full sm:w-auto">
                  Register institution
                  <ArrowRight className="size-4" />
                </Button>
              </Link>
              <a href="#services" className="w-full sm:w-auto">
                <Button variant="outline" size="lg" className="w-full sm:w-auto">
                  View services
                </Button>
              </a>
            </div>
          </div>
          <HeroPanel />
        </div>
      </section>

      <section className="border-b border-border bg-card">
        <div className="mx-auto grid max-w-6xl gap-px bg-border px-0 sm:grid-cols-2 lg:grid-cols-4">
          {[
            ["One platform", "Academics, students, placements"],
            ["Team workspaces", "Focused access for every department"],
            ["Career support", "Guidance for student readiness"],
            ["Outcome tracking", "Clear reports for leadership"],
          ].map(([value, label]) => (
            <div key={value} className="bg-card px-6 py-6">
              <div className="text-lg font-semibold">{value}</div>
              <div className="mt-1 text-sm text-muted-foreground">{label}</div>
            </div>
          ))}
        </div>
      </section>

      <section id="services" className="border-b border-border bg-muted/30">
        <div className="mx-auto max-w-6xl px-6 py-20 md:py-24">
          <FadeUp>
            <p className="text-xs font-semibold uppercase tracking-widest text-accent">What We Do</p>
            <h2 className="mt-3 max-w-3xl text-3xl font-semibold tracking-tight md:text-4xl">
              We help educational institutions move from scattered work to one connected platform.
            </h2>
            <p className="mt-4 max-w-2xl text-base leading-7 text-muted-foreground">
              Campus360 brings essential institution workflows together so teams can work faster,
              students stay informed, and leadership sees measurable progress.
            </p>
          </FadeUp>

          <Stagger className="mt-12 grid gap-px overflow-hidden rounded-lg border border-border bg-border sm:grid-cols-2 lg:grid-cols-3">
            {SERVICES.map((service) => (
              <StaggerItem key={service.title} className="bg-card p-7">
                <div className="grid size-10 place-items-center rounded-md bg-accent/10 text-accent">
                  <service.icon className="size-5" />
                </div>
                <h3 className="mt-5 text-base font-semibold">{service.title}</h3>
                <p className="mt-2 text-sm leading-6 text-muted-foreground">{service.body}</p>
              </StaggerItem>
            ))}
          </Stagger>
        </div>
      </section>

      <section className="border-b border-border">
        <div className="mx-auto grid max-w-6xl gap-10 px-6 py-20 md:grid-cols-[0.9fr_1.1fr] md:py-24">
          <FadeUp>
            <p className="text-xs font-semibold uppercase tracking-widest text-accent">Who We Serve</p>
            <h2 className="mt-3 text-3xl font-semibold tracking-tight md:text-4xl">
              Built for the people who keep a campus moving.
            </h2>
            <p className="mt-4 text-base leading-7 text-muted-foreground">
              The platform supports decision makers, administrators, placement teams, faculty, and
              students with focused workspaces and reliable information.
            </p>
          </FadeUp>

          <div className="grid gap-4">
            {AUDIENCES.map((audience, index) => (
              <FadeUp key={audience.title} delay={index * 0.08}>
                <div className="flex gap-4 rounded-lg border border-border bg-card p-5">
                  <div className="grid size-11 shrink-0 place-items-center rounded-md bg-muted text-accent">
                    <audience.icon className="size-5" />
                  </div>
                  <div>
                    <h3 className="font-semibold">{audience.title}</h3>
                    <p className="mt-1 text-sm leading-6 text-muted-foreground">{audience.body}</p>
                  </div>
                </div>
              </FadeUp>
            ))}
          </div>
        </div>
      </section>

      <section className="border-b border-border bg-card">
        <div className="mx-auto grid max-w-6xl gap-10 px-6 py-20 md:grid-cols-[1.05fr_0.95fr] md:items-center md:py-24">
          <FadeUp>
            <p className="text-xs font-semibold uppercase tracking-widest text-accent">Business Value</p>
            <h2 className="mt-3 text-3xl font-semibold tracking-tight md:text-4xl">
              Better coordination, stronger student support, and clearer outcomes.
            </h2>
            <div className="mt-8 grid gap-3">
              {OUTCOMES.map((outcome) => (
                <div key={outcome} className="flex items-start gap-3 text-sm leading-6">
                  <CheckCircle2 className="mt-0.5 size-5 shrink-0 text-success" />
                  <span>{outcome}</span>
                </div>
              ))}
            </div>
          </FadeUp>

          <FadeUp delay={0.1}>
            <div className="rounded-lg border border-border bg-background p-5 shadow-sm">
              <div className="flex items-center justify-between border-b border-border pb-4">
                <div>
                  <p className="text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                    Operations Snapshot
                  </p>
                  <h3 className="mt-1 font-semibold">Institution dashboard</h3>
                </div>
                <LineChart className="size-5 text-accent" />
              </div>
              <div className="mt-5 grid grid-cols-2 gap-3">
                {[
                  ["Students", "Central records"],
                  ["Placements", "Live activity"],
                  ["Companies", "Recruiter pipeline"],
                  ["Reports", "Outcome visibility"],
                ].map(([title, body]) => (
                  <div key={title} className="rounded-md border border-border bg-card p-4">
                    <p className="font-semibold">{title}</p>
                    <p className="mt-1 text-xs text-muted-foreground">{body}</p>
                  </div>
                ))}
              </div>
              <div className="mt-5 rounded-md border border-border bg-card p-4">
                <div className="mb-3 flex items-center justify-between">
                  <span className="text-sm font-medium">Placement progress</span>
                  <span className="text-xs text-accent">Current cycle</span>
                </div>
                <div className="h-2 overflow-hidden rounded-full bg-muted">
                  <div className="h-full w-[72%] rounded-full bg-accent" />
                </div>
              </div>
            </div>
          </FadeUp>
        </div>
      </section>

      <section className="border-b border-border bg-muted/30">
        <div className="mx-auto max-w-6xl px-6 py-20 md:py-24">
          <FadeUp>
            <p className="text-xs font-semibold uppercase tracking-widest text-accent">How It Works</p>
            <h2 className="mt-3 max-w-3xl text-3xl font-semibold tracking-tight md:text-4xl">
              A simple path from onboarding to daily operations.
            </h2>
          </FadeUp>
          <div className="mt-12 grid gap-4 md:grid-cols-3">
            {STEPS.map((step, index) => (
              <FadeUp key={step.n} delay={index * 0.08}>
                <div className="h-full rounded-lg border border-border bg-card p-7">
                  <span className="text-sm font-semibold text-accent">{step.n}</span>
                  <h3 className="mt-4 text-lg font-semibold">{step.title}</h3>
                  <p className="mt-2 text-sm leading-6 text-muted-foreground">{step.body}</p>
                </div>
              </FadeUp>
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
                  Ready to modernize your campus operations?
                </h2>
                <p className="mt-4 max-w-2xl text-base leading-7 text-background/75">
                  Register your institution and start building a connected experience for administrators,
                  placement teams, and students.
                </p>
              </div>
              <Link to="/register">
                <Button variant="accent" size="lg">
                  Start registration
                  <ArrowRight className="size-4" />
                </Button>
              </Link>
            </div>
          </FadeUp>
        </div>
      </section>

      <footer className="border-t border-border">
        <div className="mx-auto flex max-w-6xl flex-col gap-4 px-6 py-8 text-sm text-muted-foreground md:flex-row md:items-center md:justify-between">
          <span>Campus360 - Campus management and placement services</span>
          <div className="flex flex-wrap gap-x-6 gap-y-2">
            <span>Institution operations</span>
            <span>Student success</span>
            <span>Placement outcomes</span>
          </div>
        </div>
      </footer>
    </div>
  );
}

function HeroPanel() {
  return (
    <div className="hidden min-w-0 lg:block">
      <div className="rounded-lg border border-border bg-card/95 p-5 shadow-2xl">
        <div className="flex items-center justify-between border-b border-border pb-4">
          <div>
            <p className="text-xs font-semibold uppercase tracking-wide text-muted-foreground">
              Institution command center
            </p>
            <h2 className="mt-1 text-lg font-semibold">Operations overview</h2>
          </div>
          <div className="grid size-10 place-items-center rounded-md bg-accent text-accent-foreground">
            <Building2 className="size-5" />
          </div>
        </div>

        <div className="mt-5 grid grid-cols-2 gap-3">
          {[
            ["Students", "2,840"],
            ["Open roles", "36"],
            ["Companies", "128"],
            ["Offers", "412"],
          ].map(([label, value]) => (
            <div key={label} className="rounded-md border border-border bg-background p-4">
              <div className="text-2xl font-semibold">{value}</div>
              <div className="mt-1 text-xs text-muted-foreground">{label}</div>
            </div>
          ))}
        </div>

        <div className="mt-5 rounded-md border border-border bg-background p-4">
          <div className="mb-4 flex items-center justify-between">
            <span className="text-sm font-semibold">Placement cycle</span>
            <span className="text-xs text-accent">Current year</span>
          </div>
          {[
            ["Applications received", "82%"],
            ["Students shortlisted", "58%"],
            ["Interviews scheduled", "44%"],
            ["Offers completed", "31%"],
          ].map(([label, width]) => (
            <div key={label} className="mb-4 last:mb-0">
              <div className="mb-1 flex justify-between gap-4 text-xs">
                <span>{label}</span>
                <span className="text-muted-foreground">{width}</span>
              </div>
              <div className="h-2 overflow-hidden rounded-full bg-muted">
                <div className="h-full rounded-full bg-accent" style={{ width }} />
              </div>
            </div>
          ))}
        </div>

        <div className="mt-5 grid gap-3">
          {[
            [CalendarCheck2, "Academic setup reviewed"],
            [MessageSquareText, "Student guidance shared"],
            [ClipboardCheck, "Offer responses tracked"],
          ].map(([Icon, label]) => (
            <div key={label as string} className="flex items-center gap-3 rounded-md border border-border bg-background p-3">
              <Icon className="size-4 text-accent" />
              <span className="text-sm font-medium">{label as string}</span>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
