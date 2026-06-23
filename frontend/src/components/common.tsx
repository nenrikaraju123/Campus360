import { motion } from "motion/react";
import { Loader2, Inbox, AlertCircle, type LucideIcon } from "lucide-react";
import { ApiError } from "@/lib/api/client";
import { cn } from "@/lib/utils";

export function PageHeader({
  title,
  description,
  actions,
}: {
  title: string;
  description?: string;
  actions?: React.ReactNode;
}) {
  return (
    <div className="mb-8 flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
      <div>
        <h1 className="text-2xl font-semibold tracking-tight">{title}</h1>
        {description && <p className="mt-1 text-sm text-muted-foreground">{description}</p>}
      </div>
      {actions && <div className="flex shrink-0 items-center gap-2">{actions}</div>}
    </div>
  );
}

export function StatCard({
  label,
  value,
  icon: Icon,
  hint,
  index = 0,
}: {
  label: string;
  value: string | number;
  icon?: LucideIcon;
  hint?: string;
  index?: number;
}) {
  return (
    <motion.div
      initial={{ opacity: 0, y: 12 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.4, delay: index * 0.06, ease: [0.16, 1, 0.3, 1] }}
      className="rounded-lg border border-border bg-card p-5"
    >
      <div className="flex items-center justify-between">
        <span className="text-[10px] font-semibold uppercase tracking-wider text-muted-foreground">
          {label}
        </span>
        {Icon && <Icon className="size-4 text-accent" />}
      </div>
      <div className="mt-2 font-mono text-2xl font-semibold">{value}</div>
      {hint && <div className="mt-1 text-xs text-muted-foreground">{hint}</div>}
    </motion.div>
  );
}

export function TabBar<T extends string>({
  tabs,
  value,
  onChange,
  layoutId = "tabbar",
}: {
  tabs: { value: T; label: string }[];
  value: T;
  onChange: (v: T) => void;
  layoutId?: string;
}) {
  return (
    <div className="inline-flex flex-wrap gap-1 rounded-lg border border-border bg-muted/50 p-1">
      {tabs.map((t) => (
        <button
          key={t.value}
          onClick={() => onChange(t.value)}
          className={cn(
            "relative rounded-md px-3.5 py-1.5 text-sm font-medium transition-colors",
            value === t.value ? "text-foreground" : "text-muted-foreground hover:text-foreground",
          )}
        >
          {value === t.value && (
            <motion.span
              layoutId={layoutId}
              className="absolute inset-0 rounded-md bg-card shadow-sm"
              transition={{ type: "spring", stiffness: 400, damping: 32 }}
            />
          )}
          <span className="relative z-10">{t.label}</span>
        </button>
      ))}
    </div>
  );
}

export function EmptyState({
  icon: Icon = Inbox,
  title,
  body,
  action,
}: {
  icon?: LucideIcon;
  title: string;
  body?: string;
  action?: React.ReactNode;
}) {
  return (
    <div className="flex flex-col items-center justify-center rounded-lg border border-dashed border-border bg-card/50 px-6 py-16 text-center">
      <div className="grid size-12 place-items-center rounded-full bg-muted text-muted-foreground">
        <Icon className="size-6" />
      </div>
      <h3 className="mt-4 font-medium">{title}</h3>
      {body && <p className="mt-1 max-w-sm text-sm text-muted-foreground">{body}</p>}
      {action && <div className="mt-5">{action}</div>}
    </div>
  );
}

export function DataState({
  isLoading,
  error,
  isEmpty,
  emptyTitle = "Nothing here yet",
  emptyBody,
  children,
}: {
  isLoading: boolean;
  error?: unknown;
  isEmpty?: boolean;
  emptyTitle?: string;
  emptyBody?: string;
  children: React.ReactNode;
}) {
  if (isLoading) {
    return (
      <div className="flex items-center justify-center py-20 text-muted-foreground">
        <Loader2 className="size-6 animate-spin" />
      </div>
    );
  }
  if (error) {
    return (
      <div className="flex items-center gap-3 rounded-lg border border-destructive/30 bg-destructive/5 px-4 py-4 text-sm text-destructive">
        <AlertCircle className="size-5 shrink-0" />
        {error instanceof ApiError ? error.detail : "Something went wrong loading this data."}
      </div>
    );
  }
  if (isEmpty) {
    return <EmptyState title={emptyTitle} body={emptyBody} />;
  }
  return <>{children}</>;
}

export function moneyINR(value?: number | null): string {
  if (value == null || value === 0) return "—";
  return "₹" + Number(value).toLocaleString("en-IN");
}

export function shortDate(value?: string | null): string {
  if (!value) return "—";
  return new Date(value).toLocaleDateString("en-IN", {
    day: "2-digit",
    month: "short",
    year: "numeric",
  });
}
