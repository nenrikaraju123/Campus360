import { useState } from "react";
import { useNavigate } from "@tanstack/react-router";
import { motion } from "motion/react";
import { Loader2 } from "lucide-react";
import { toast } from "sonner";
import { Input } from "@/components/ui/input";
import { Field } from "@/components/ui/field";
import { Button } from "@/components/ui/button";
import { login } from "@/lib/api/auth";
import { ApiError } from "@/lib/api/client";
import { homeRouteForRoles } from "@/lib/auth/roles";
import { cn } from "@/lib/utils";

type Tab = "institution" | "platform";

export function LoginForm() {
  const [tab, setTab] = useState<Tab>("institution");
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const navigate = useNavigate();

  async function handleSubmit(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault();
    setError(null);
    const data = Object.fromEntries(new FormData(e.currentTarget)) as Record<string, string>;
    setSubmitting(true);
    try {
      const res = await login({
        tenantCode: tab === "institution" ? data.tenantCode : undefined,
        email: data.email,
        password: data.password,
      });
      if (res.mustChangePassword) {
        navigate({ to: "/change-password" });
      } else {
        navigate({ to: homeRouteForRoles(res.roles) });
        toast.success("Signed in");
      }
    } catch (err) {
      setError(err instanceof ApiError ? err.detail : "Something went wrong. Please try again.");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="space-y-6">
      <div className="grid grid-cols-2 gap-1 rounded-lg border border-border bg-muted/50 p-1">
        {(["institution", "platform"] as Tab[]).map((t) => (
          <button
            key={t}
            type="button"
            onClick={() => {
              setTab(t);
              setError(null);
            }}
            className={cn(
              "relative rounded-md px-3 py-2 text-sm font-medium transition-colors",
              tab === t ? "text-foreground" : "text-muted-foreground hover:text-foreground",
            )}
          >
            {tab === t && (
              <motion.span
                layoutId="login-tab"
                className="absolute inset-0 rounded-md bg-card shadow-sm"
                transition={{ type: "spring", stiffness: 400, damping: 32 }}
              />
            )}
            <span className="relative z-10">
              {t === "institution" ? "Institution user" : "Platform admin"}
            </span>
          </button>
        ))}
      </div>

      <form onSubmit={handleSubmit} className="space-y-4" noValidate>
        {tab === "institution" && (
          <Field label="Tenant code" hint="Your institution's code, e.g. AIT">
            <Input name="tenantCode" placeholder="AIT" required className="font-mono uppercase" />
          </Field>
        )}
        <Field label="Email">
          <Input name="email" type="email" placeholder="you@example.edu" required autoComplete="username" />
        </Field>
        <Field label="Password">
          <Input
            name="password"
            type="password"
            placeholder="••••••••"
            required
            autoComplete="current-password"
          />
        </Field>

        {error && (
          <motion.p
            initial={{ opacity: 0, y: -4 }}
            animate={{ opacity: 1, y: 0 }}
            className="rounded-md border border-destructive/30 bg-destructive/10 px-3 py-2 text-sm text-destructive"
          >
            {error}
          </motion.p>
        )}

        <Button type="submit" variant="accent" size="lg" className="w-full" disabled={submitting}>
          {submitting && <Loader2 className="size-4 animate-spin" />}
          Sign in
        </Button>
      </form>
    </div>
  );
}
