import { useState } from "react";
import { useNavigate } from "@tanstack/react-router";
import { Loader2 } from "lucide-react";
import { toast } from "sonner";
import { z } from "zod";
import { Input } from "@/components/ui/input";
import { Field } from "@/components/ui/field";
import { Button } from "@/components/ui/button";
import { changePassword } from "@/lib/api/auth";
import { ApiError } from "@/lib/api/client";
import { useAuthStore } from "@/lib/auth/store";
import { homeRouteForRoles } from "@/lib/auth/roles";

const schema = z
  .object({
    currentPassword: z.string().min(1, "Required"),
    newPassword: z.string().min(8, "At least 8 characters"),
    confirm: z.string(),
  })
  .refine((d) => d.newPassword === d.confirm, {
    path: ["confirm"],
    message: "Passwords do not match",
  });

type Errors = Partial<Record<"currentPassword" | "newPassword" | "confirm", string>>;

export function ChangePasswordForm() {
  const [errors, setErrors] = useState<Errors>({});
  const [submitting, setSubmitting] = useState(false);
  const navigate = useNavigate();

  async function handleSubmit(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault();
    const data = Object.fromEntries(new FormData(e.currentTarget));
    const parsed = schema.safeParse(data);
    if (!parsed.success) {
      const fe: Errors = {};
      for (const issue of parsed.error.issues) fe[issue.path[0] as keyof Errors] = issue.message;
      setErrors(fe);
      return;
    }
    setErrors({});
    setSubmitting(true);
    try {
      await changePassword(parsed.data.currentPassword, parsed.data.newPassword);
      toast.success("Password updated");
      const roles = useAuthStore.getState().user?.roles;
      navigate({ to: homeRouteForRoles(roles) });
    } catch (err) {
      toast.error(err instanceof ApiError ? err.detail : "Could not update password");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-4" noValidate>
      <Field label="Current / temporary password" error={errors.currentPassword}>
        <Input name="currentPassword" type="password" autoComplete="current-password" required />
      </Field>
      <Field label="New password" error={errors.newPassword} hint="Minimum 8 characters">
        <Input name="newPassword" type="password" autoComplete="new-password" required />
      </Field>
      <Field label="Confirm new password" error={errors.confirm}>
        <Input name="confirm" type="password" autoComplete="new-password" required />
      </Field>
      <Button type="submit" variant="accent" size="lg" className="w-full" disabled={submitting}>
        {submitting && <Loader2 className="size-4 animate-spin" />}
        Update password
      </Button>
    </form>
  );
}
