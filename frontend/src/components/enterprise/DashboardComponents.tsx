import React from "react";
import { cn } from "@/lib/utils";

interface DashboardPanelProps {
  title: string;
  action?: React.ReactNode;
  children: React.ReactNode;
  className?: string;
}

export function DashboardPanel({ title, action, children, className }: DashboardPanelProps) {
  return (
    <div className={cn("flex flex-col rounded-xl border border-border bg-card shadow-sm", className)}>
      <div className="flex items-center justify-between border-b border-border px-5 py-4">
        <h3 className="text-base font-semibold leading-6 text-foreground">{title}</h3>
        {action && <div>{action}</div>}
      </div>
      <div className="flex-1 p-5">{children}</div>
    </div>
  );
}

export function MetricStrip({ children, className }: { children: React.ReactNode; className?: string }) {
  return (
    <div className={cn("grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4", className)}>
      {children}
    </div>
  );
}
