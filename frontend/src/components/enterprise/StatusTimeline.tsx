import React from "react";
import { cn } from "@/lib/utils";
import { Check, Circle } from "lucide-react";

export interface TimelineStep {
  label: string;
  description?: string;
  date?: string;
  status: "complete" | "current" | "upcoming";
}

export function StatusTimeline({ steps, className }: { steps: TimelineStep[]; className?: string }) {
  return (
    <nav aria-label="Progress" className={className}>
      <ol role="list" className="overflow-hidden">
        {steps.map((step, stepIdx) => (
          <li key={step.label} className={cn(stepIdx !== steps.length - 1 ? "pb-10" : "", "relative")}>
            {step.status === "complete" ? (
              <>
                {stepIdx !== steps.length - 1 ? (
                  <div className="absolute left-4 top-4 -ml-px mt-0.5 h-full w-0.5 bg-accent" aria-hidden="true" />
                ) : null}
                <div className="group relative flex items-start">
                  <span className="flex h-9 items-center">
                    <span className="relative z-10 flex h-8 w-8 items-center justify-center rounded-full bg-accent hover:bg-accent/80">
                      <Check className="h-5 w-5 text-accent-foreground" aria-hidden="true" />
                    </span>
                  </span>
                  <span className="ml-4 flex min-w-0 flex-col">
                    <span className="text-sm font-medium tracking-wide">{step.label}</span>
                    {step.description && <span className="text-sm text-muted-foreground">{step.description}</span>}
                    {step.date && <span className="text-xs text-muted-foreground">{step.date}</span>}
                  </span>
                </div>
              </>
            ) : step.status === "current" ? (
              <>
                {stepIdx !== steps.length - 1 ? (
                  <div className="absolute left-4 top-4 -ml-px mt-0.5 h-full w-0.5 bg-border" aria-hidden="true" />
                ) : null}
                <div className="group relative flex items-start" aria-current="step">
                  <span className="flex h-9 items-center" aria-hidden="true">
                    <span className="relative z-10 flex h-8 w-8 items-center justify-center rounded-full border-2 border-accent bg-background">
                      <span className="h-2.5 w-2.5 rounded-full bg-accent" />
                    </span>
                  </span>
                  <span className="ml-4 flex min-w-0 flex-col">
                    <span className="text-sm font-medium text-accent tracking-wide">{step.label}</span>
                    {step.description && <span className="text-sm text-muted-foreground">{step.description}</span>}
                    {step.date && <span className="text-xs text-muted-foreground">{step.date}</span>}
                  </span>
                </div>
              </>
            ) : (
              <>
                {stepIdx !== steps.length - 1 ? (
                  <div className="absolute left-4 top-4 -ml-px mt-0.5 h-full w-0.5 bg-border" aria-hidden="true" />
                ) : null}
                <div className="group relative flex items-start">
                  <span className="flex h-9 items-center" aria-hidden="true">
                    <span className="relative z-10 flex h-8 w-8 items-center justify-center rounded-full border-2 border-border bg-background hover:border-muted-foreground">
                      <Circle className="h-2.5 w-2.5 text-transparent" />
                    </span>
                  </span>
                  <span className="ml-4 flex min-w-0 flex-col">
                    <span className="text-sm font-medium text-muted-foreground tracking-wide">{step.label}</span>
                    {step.description && <span className="text-sm text-muted-foreground">{step.description}</span>}
                  </span>
                </div>
              </>
            )}
          </li>
        ))}
      </ol>
    </nav>
  );
}
