import { motion } from "motion/react";
import { Sparkles } from "lucide-react";
import type { ReadinessReport } from "@/lib/api/entities";
import { StatusBadge } from "@/components/ui/badge";
import { Card, CardContent } from "@/components/ui/card";

export function ReadinessCard({ report }: { report: ReadinessReport }) {
  const tone =
    report.band === "STRONG" ? "var(--success)" : report.band === "DEVELOPING" ? "#f59e0b" : "var(--destructive)";
  return (
    <Card>
      <CardContent className="p-6">
        <div className="flex flex-col gap-6 sm:flex-row sm:items-center">
          {/* Score ring */}
          <div className="relative grid size-28 shrink-0 place-items-center">
            <svg className="size-28 -rotate-90" viewBox="0 0 100 100">
              <circle cx="50" cy="50" r="42" fill="none" stroke="var(--muted)" strokeWidth="8" />
              <motion.circle
                cx="50" cy="50" r="42" fill="none" stroke={tone} strokeWidth="8" strokeLinecap="round"
                strokeDasharray={264}
                initial={{ strokeDashoffset: 264 }}
                animate={{ strokeDashoffset: 264 - (264 * report.score) / 100 }}
                transition={{ duration: 0.9, ease: [0.16, 1, 0.3, 1] }}
              />
            </svg>
            <div className="absolute text-center">
              <div className="font-mono text-2xl font-semibold">{report.score}</div>
              <div className="text-[10px] uppercase tracking-wide text-muted-foreground">/ 100</div>
            </div>
          </div>

          <div className="flex-1">
            <div className="flex items-center gap-2">
              <h3 className="font-semibold">Placement readiness</h3>
              <StatusBadge status={report.band} />
            </div>
            <ul className="mt-3 space-y-1.5">
              {report.factors.map((f) => (
                <li key={f} className="flex items-center gap-2 text-sm text-muted-foreground">
                  <span className="size-1.5 rounded-full bg-accent" />
                  {f}
                </li>
              ))}
            </ul>
          </div>
        </div>

        <div className="mt-5 rounded-lg border border-border bg-muted/30 p-4">
          <div className="mb-2 flex items-center gap-2 text-xs font-semibold uppercase tracking-wide text-accent">
            <Sparkles className="size-3.5" /> Coaching {report.aiLive ? "(AI)" : "(offline)"}
          </div>
          <p className="whitespace-pre-wrap text-sm text-muted-foreground">{report.coaching}</p>
        </div>
      </CardContent>
    </Card>
  );
}
