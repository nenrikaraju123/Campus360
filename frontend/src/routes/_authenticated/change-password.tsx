import { createFileRoute } from "@tanstack/react-router";
import { motion } from "motion/react";
import { KeyRound } from "lucide-react";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { ChangePasswordForm } from "@/components/forms/ChangePasswordForm";
import { ThemeToggle } from "@/components/layout/ThemeToggle";
import { Brand } from "@/components/layout/Brand";
import { useAuthStore } from "@/lib/auth/store";

export const Route = createFileRoute("/_authenticated/change-password")({
  component: ChangePasswordPage,
});

function ChangePasswordPage() {
  const mustChange = useAuthStore((s) => s.user?.mustChangePassword);
  return (
    <div className="flex min-h-screen flex-col">
      <div className="flex items-center justify-between p-6">
        <Brand />
        <ThemeToggle />
      </div>
      <div className="flex flex-1 items-center justify-center px-6 pb-16">
        <motion.div
          initial={{ opacity: 0, scale: 0.97 }}
          animate={{ opacity: 1, scale: 1 }}
          transition={{ duration: 0.35, ease: [0.16, 1, 0.3, 1] }}
          className="w-full max-w-md"
        >
          <Card>
            <CardHeader>
              <div className="grid size-11 place-items-center rounded-lg bg-accent/10 text-accent">
                <KeyRound className="size-5" />
              </div>
              <CardTitle className="mt-3">
                {mustChange ? "Set a new password" : "Change password"}
              </CardTitle>
              <CardDescription>
                {mustChange
                  ? "For security, you must replace your temporary password before continuing."
                  : "Choose a new password for your account."}
              </CardDescription>
            </CardHeader>
            <CardContent>
              <ChangePasswordForm />
            </CardContent>
          </Card>
        </motion.div>
      </div>
    </div>
  );
}
