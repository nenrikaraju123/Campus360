import { cn } from "@/lib/utils";

type Tone = "neutral" | "accent" | "success" | "warning" | "danger" | "info";

const tones: Record<Tone, string> = {
  neutral: "bg-muted text-muted-foreground",
  accent: "bg-accent/10 text-accent",
  success: "bg-success/10 text-success",
  warning: "bg-amber-500/10 text-amber-600 dark:text-amber-400",
  danger: "bg-destructive/10 text-destructive",
  info: "bg-blue-500/10 text-blue-600 dark:text-blue-400",
};

export function Badge({
  children,
  tone = "neutral",
  className,
}: {
  children: React.ReactNode;
  tone?: Tone;
  className?: string;
}) {
  return (
    <span
      className={cn(
        "inline-flex items-center rounded-full px-2.5 py-0.5 font-mono text-[11px] font-medium uppercase tracking-wide",
        tones[tone],
        className,
      )}
    >
      {children}
    </span>
  );
}

/** Maps common backend status strings to a coloured badge. */
export function StatusBadge({ status }: { status: string }) {
  const map: Record<string, Tone> = {
    PENDING: "warning",
    APPROVED: "success",
    ACTIVE: "success",
    OPEN: "success",
    ACCEPTED: "success",
    REJECTED: "danger",
    SUSPENDED: "danger",
    DECLINED: "danger",
    CLOSED: "neutral",
    DRAFT: "neutral",
    WITHDRAWN: "neutral",
    APPLIED: "info",
    SHORTLISTED: "info",
    OFFERED: "accent",
    EXTENDED: "accent",
    STRONG: "success",
    DEVELOPING: "warning",
    AT_RISK: "danger",
  };
  return <Badge tone={map[status] ?? "neutral"}>{status}</Badge>;
}
