import { motion } from "motion/react";
import { ArrowUpRight } from "lucide-react";
import { Card, CardContent } from "@/components/ui/card";
import { useAuthStore } from "@/lib/auth/store";

interface Upcoming {
  title: string;
  body: string;
  phase: string;
}

export function Welcome({
  title,
  subtitle,
  upcoming,
}: {
  title: string;
  subtitle: string;
  upcoming: Upcoming[];
}) {
  const user = useAuthStore((s) => s.user);

  return (
    <div className="mx-auto max-w-5xl space-y-8">
      <div>
        <p className="font-mono text-xs uppercase tracking-widest text-accent">{subtitle}</p>
        <h1 className="mt-2 text-3xl font-semibold tracking-tight">{title}</h1>
      </div>

      {/* Identity card */}
      <Card>
        <CardContent className="grid gap-px overflow-hidden rounded-lg bg-border p-0 sm:grid-cols-3">
          {[
            ["Email", user?.email ?? "—"],
            ["Roles", user?.roles.join(" · ") ?? "—"],
            ["Tenant", user?.tenantId != null ? `#${user.tenantId}` : "Platform"],
          ].map(([label, value]) => (
            <div key={label} className="bg-card px-6 py-5">
              <div className="text-[10px] font-semibold uppercase tracking-wider text-muted-foreground">
                {label}
              </div>
              <div className="mt-1.5 truncate font-mono text-sm">{value}</div>
            </div>
          ))}
        </CardContent>
      </Card>

      {/* Upcoming modules */}
      <div>
        <h2 className="text-sm font-medium text-muted-foreground">Coming in the next phases</h2>
        <div className="mt-4 grid gap-4 sm:grid-cols-2">
          {upcoming.map((u, i) => (
            <motion.div
              key={u.title}
              initial={{ opacity: 0, y: 12 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.4, delay: i * 0.06, ease: [0.16, 1, 0.3, 1] }}
            >
              <Card className="group h-full transition-colors hover:border-accent/40">
                <CardContent className="flex h-full flex-col p-6">
                  <div className="flex items-center justify-between">
                    <span className="font-mono text-[11px] uppercase tracking-wide text-accent">
                      {u.phase}
                    </span>
                    <ArrowUpRight className="size-4 text-muted-foreground transition-transform group-hover:translate-x-0.5 group-hover:-translate-y-0.5" />
                  </div>
                  <h3 className="mt-3 font-semibold">{u.title}</h3>
                  <p className="mt-1.5 text-sm text-muted-foreground">{u.body}</p>
                </CardContent>
              </Card>
            </motion.div>
          ))}
        </div>
      </div>
    </div>
  );
}
