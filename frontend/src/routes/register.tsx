import { useState } from "react";
import { createFileRoute, Link } from "@tanstack/react-router";
import { motion } from "motion/react";
import { CheckCircle2, ArrowLeft, Clock } from "lucide-react";
import { PublicNav } from "@/components/layout/PublicNav";
import { RegistrationForm } from "@/components/forms/RegistrationForm";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import type { RegistrationAck } from "@/lib/api/types";

export const Route = createFileRoute("/register")({
  component: RegisterPage,
});

function RegisterPage() {
  const [ack, setAck] = useState<RegistrationAck | null>(null);

  return (
    <div className="min-h-screen">
      <PublicNav />
      <div className="bg-grid pointer-events-none absolute inset-x-0 top-16 h-64 [mask-image:radial-gradient(ellipse_at_top,black,transparent_70%)]" />
      <main className="relative mx-auto grid max-w-6xl gap-12 px-6 py-14 lg:grid-cols-[1fr_minmax(0,520px)]">
        {/* Intro */}
        <motion.div
          initial={{ opacity: 0, x: -16 }}
          animate={{ opacity: 1, x: 0 }}
          transition={{ duration: 0.4, ease: [0.16, 1, 0.3, 1] }}
          className="hidden lg:block"
        >
          <p className="font-mono text-xs uppercase tracking-widest text-accent">Onboarding</p>
          <h1 className="mt-3 text-4xl font-semibold tracking-tight">Register your institution</h1>
          <p className="mt-4 max-w-md text-muted-foreground">
            Submit your details and the platform administrator will review and provision your tenant.
            You'll receive admin credentials and a tenant code to sign in with.
          </p>
          <ul className="mt-8 space-y-3 text-sm text-muted-foreground">
            {[
              "Your tenant code becomes your sign-in namespace",
              "The first admin is created automatically on approval",
              "A temporary password is issued and must be changed on first login",
            ].map((t) => (
              <li key={t} className="flex items-start gap-2.5">
                <CheckCircle2 className="mt-0.5 size-4 shrink-0 text-accent" />
                {t}
              </li>
            ))}
          </ul>
        </motion.div>

        {/* Form / success */}
        <div>
          {ack ? (
            <motion.div
              initial={{ opacity: 0, scale: 0.96 }}
              animate={{ opacity: 1, scale: 1 }}
              transition={{ duration: 0.35, ease: [0.16, 1, 0.3, 1] }}
            >
              <Card>
                <CardContent className="flex flex-col items-center px-8 py-12 text-center">
                  <div className="grid size-14 place-items-center rounded-full bg-accent/10 text-accent">
                    <Clock className="size-7" />
                  </div>
                  <h2 className="mt-6 text-xl font-semibold">Registration submitted</h2>
                  <p className="mt-2 max-w-sm text-sm text-muted-foreground">{ack.message}</p>
                  <div className="mt-5 rounded-md border border-border bg-muted/50 px-4 py-2 font-mono text-xs">
                    Request #{ack.id} · <span className="text-accent">{ack.status}</span>
                  </div>
                  <Link to="/" className="mt-8">
                    <Button variant="outline">
                      <ArrowLeft className="size-4" />
                      Back to home
                    </Button>
                  </Link>
                </CardContent>
              </Card>
            </motion.div>
          ) : (
            <Card>
              <CardHeader>
                <CardTitle>Institution details</CardTitle>
                <CardDescription>All fields marked are required for review.</CardDescription>
              </CardHeader>
              <CardContent>
                <RegistrationForm onSuccess={setAck} />
              </CardContent>
            </Card>
          )}
        </div>
      </main>
    </div>
  );
}
