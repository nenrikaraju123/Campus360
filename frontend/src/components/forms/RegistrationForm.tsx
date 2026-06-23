import { useState } from "react";
import { z } from "zod";
import { toast } from "sonner";
import { Loader2 } from "lucide-react";
import { Input, Textarea } from "@/components/ui/input";
import { Field } from "@/components/ui/field";
import { Button } from "@/components/ui/button";
import { submitRegistration } from "@/lib/api/registrations";
import { ApiError } from "@/lib/api/client";
import type { RegistrationAck } from "@/lib/api/types";

const schema = z.object({
  institutionName: z.string().min(2, "Institution name is required"),
  institutionCode: z
    .string()
    .min(2, "Code is required")
    .max(40)
    .regex(/^[A-Za-z0-9_-]+$/, "Letters, numbers, - and _ only"),
  type: z.enum(["UNIVERSITY", "COLLEGE", "INSTITUTE"]),
  adminFullName: z.string().min(2, "Admin name is required"),
  adminEmail: z.string().email("Enter a valid email"),
  contactPhone: z.string().optional(),
  message: z.string().max(1000).optional(),
});

type Errors = Partial<Record<keyof z.infer<typeof schema>, string>>;

export function RegistrationForm({ onSuccess }: { onSuccess: (ack: RegistrationAck) => void }) {
  const [errors, setErrors] = useState<Errors>({});
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault();
    const data = Object.fromEntries(new FormData(e.currentTarget));
    const parsed = schema.safeParse(data);
    if (!parsed.success) {
      const fieldErrors: Errors = {};
      for (const issue of parsed.error.issues) {
        fieldErrors[issue.path[0] as keyof Errors] = issue.message;
      }
      setErrors(fieldErrors);
      return;
    }
    setErrors({});
    setSubmitting(true);
    try {
      const ack = await submitRegistration(parsed.data);
      onSuccess(ack);
    } catch (err) {
      toast.error(err instanceof ApiError ? err.detail : "Could not submit registration");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-5" noValidate>
      <div className="grid gap-5 sm:grid-cols-2">
        <Field label="Institution name" error={errors.institutionName}>
          <Input name="institutionName" placeholder="Acme Institute of Technology" />
        </Field>
        <Field label="Tenant code" error={errors.institutionCode} hint="Used at login, e.g. AIT">
          <Input name="institutionCode" placeholder="AIT" className="font-mono uppercase" />
        </Field>
      </div>

      <Field label="Type" error={errors.type}>
        <select
          name="type"
          defaultValue="UNIVERSITY"
          className="flex h-10 w-full rounded-md border border-input bg-card px-3 text-sm shadow-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
        >
          <option value="UNIVERSITY">University</option>
          <option value="COLLEGE">College</option>
          <option value="INSTITUTE">Institute</option>
        </select>
      </Field>

      <div className="grid gap-5 sm:grid-cols-2">
        <Field label="Admin full name" error={errors.adminFullName}>
          <Input name="adminFullName" placeholder="Asha Rao" />
        </Field>
        <Field label="Admin email" error={errors.adminEmail}>
          <Input name="adminEmail" type="email" placeholder="admin@ait.edu" />
        </Field>
      </div>

      <Field label="Contact phone" error={errors.contactPhone} hint="Optional">
        <Input name="contactPhone" placeholder="+91 90000 00000" />
      </Field>

      <Field label="Message" error={errors.message} hint="Anything we should know? (optional)">
        <Textarea name="message" placeholder="Tell us about your institution…" />
      </Field>

      <Button type="submit" variant="accent" size="lg" className="w-full" disabled={submitting}>
        {submitting && <Loader2 className="size-4 animate-spin" />}
        Submit for review
      </Button>
    </form>
  );
}
