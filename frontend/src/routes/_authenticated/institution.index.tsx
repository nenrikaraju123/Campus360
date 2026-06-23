import { createFileRoute, Link } from "@tanstack/react-router";
import { useQuery } from "@tanstack/react-query";
import { getDashboard, getAtRiskStudents } from "@/lib/api/analytics";
import { Card, CardContent } from "@/components/ui/card";
import { DataTable, Row, Cell } from "@/components/ui/table";
import { Badge } from "@/components/ui/badge";
import { FadeUp } from "@/components/motion/FadeUp";
import { Stagger, StaggerItem } from "@/components/motion/Stagger";
import { PageHeader, StatCard } from "@/components/common";
import { AlertCircle, Users, Layers, Briefcase, FileText, Frown, ArrowRight, Wallet, Stethoscope } from "lucide-react";

export const Route = createFileRoute("/_authenticated/institution/")({
  component: DashboardPage,
});

function DashboardPage() {
  const { data: dashboard, isLoading: dashLoading } = useQuery({
    queryKey: ["dashboard"],
    queryFn: getDashboard,
  });

  const { data: atRisk, isLoading: riskLoading } = useQuery({
    queryKey: ["atRiskStudents"],
    queryFn: getAtRiskStudents,
  });

  const links = [
    { to: "/institution/academics", label: "Academic structure", body: "Departments, programs, courses, terms, sections.", icon: Layers },
    { to: "/institution/students", label: "Students", body: "Onboard and manage student records.", icon: Users },
    { to: "/institution/placement", label: "Placements", body: "Companies, postings, offers and analytics.", icon: Briefcase },
    { to: "/institution/fees", label: "Fee Management", body: "Configure structures and bulk-generate invoices.", icon: Wallet },
    { to: "/institution/life", label: "Student Life", body: "Manage grievances and document requests.", icon: Stethoscope },
  ] as const;

  return (
    <div className="space-y-8">
      <PageHeader title="Institution workspace" description="Your campus at a glance." />

      {/* KPI Cards */}
      <Stagger className="grid gap-4 sm:grid-cols-2 lg:grid-cols-5">
        <StatCard label="Total Students" value={dashLoading ? "..." : dashboard?.totalStudents ?? 0} icon={Users} index={0} />
        <StatCard label="Departments" value={dashLoading ? "..." : dashboard?.totalDepartments ?? 0} icon={Layers} index={1} />
        <StatCard label="Open Postings" value={dashLoading ? "..." : dashboard?.openPostings ?? 0} icon={Briefcase} index={2} />
        <StatCard label="Pending Invoices" value={dashLoading ? "..." : dashboard?.pendingInvoices ?? 0} icon={FileText} index={3} />
        <StatCard label="Open Grievances" value={dashLoading ? "..." : dashboard?.openGrievances ?? 0} icon={Frown} index={4} />
      </Stagger>

      {/* Quick Links */}
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {links.map((l, i) => (
          <FadeUp key={l.to} delay={0.1 + i * 0.05}>
            <Link to={l.to}>
              <Card className="group h-full transition-colors hover:border-accent/40">
                <CardContent className="flex h-full flex-col p-6">
                  <div className="grid size-10 place-items-center rounded-lg bg-accent/10 text-accent">
                    <l.icon className="size-5" />
                  </div>
                  <h3 className="mt-4 font-semibold">{l.label}</h3>
                  <p className="mt-1 flex-1 text-sm text-muted-foreground">{l.body}</p>
                  <span className="mt-3 inline-flex items-center gap-1 text-sm font-medium text-accent">
                    Open <ArrowRight className="size-4 transition-transform group-hover:translate-x-0.5" />
                  </span>
                </CardContent>
              </Card>
            </Link>
          </FadeUp>
        ))}
      </div>

      {/* At Risk Table */}
      <FadeUp delay={0.3}>
        <div className="flex items-center gap-2 mb-4 mt-8">
          <AlertCircle className="size-5 text-destructive" />
          <h2 className="text-xl font-semibold tracking-tight">At-Risk Students</h2>
        </div>
        <DataTable columns={["Student", "Roll Number", "CGPA", "Attendance", "Backlogs", "Risk Factors"]}>
          {riskLoading ? (
            <Row>
              <Cell className="text-center text-muted-foreground py-8">
                Loading risk data...
              </Cell>
            </Row>
          ) : !atRisk || atRisk.length === 0 ? (
            <Row>
              <Cell className="text-center text-muted-foreground py-8">
                No at-risk students detected. Good job!
              </Cell>
            </Row>
          ) : (
            atRisk.map((student, i) => (
              <Row key={student.studentId} index={i}>
                <Cell className="font-medium">{student.studentName}</Cell>
                <Cell className="text-muted-foreground">{student.rollNumber}</Cell>
                <Cell>
                  <span className={student.cgpa < 5.0 ? "text-destructive font-semibold" : ""}>
                    {student.cgpa.toFixed(2)}
                  </span>
                </Cell>
                <Cell>
                  <span className={student.attendancePct < 75 ? "text-destructive font-semibold" : ""}>
                    {student.attendancePct.toFixed(1)}%
                  </span>
                </Cell>
                <Cell>
                  <span className={student.activeBacklogs > 0 ? "text-destructive font-semibold" : ""}>
                    {student.activeBacklogs}
                  </span>
                </Cell>
                <Cell>
                  <div className="flex flex-wrap gap-1">
                    {student.riskReasons.map((reason, i) => (
                      <Badge key={i} tone="danger" className="text-[10px]">
                        {reason}
                      </Badge>
                    ))}
                  </div>
                </Cell>
              </Row>
            ))
          )}
        </DataTable>
      </FadeUp>
    </div>
  );
}
