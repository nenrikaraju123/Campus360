import React from "react";
import { cn } from "@/lib/utils";

interface FormSectionProps {
  title: string;
  description?: string;
  children: React.ReactNode;
  className?: string;
}

export function FormSection({ title, description, children, className }: FormSectionProps) {
  return (
    <div className={cn("grid grid-cols-1 gap-x-8 gap-y-8 pt-8 md:grid-cols-3", className)}>
      <div className="px-4 sm:px-0">
        <h2 className="text-base font-semibold leading-7 text-foreground">{title}</h2>
        {description && (
          <p className="mt-1 text-sm leading-6 text-muted-foreground">{description}</p>
        )}
      </div>

      <div className="bg-card shadow-sm ring-1 ring-border sm:rounded-xl md:col-span-2">
        <div className="px-4 py-6 sm:p-8">
          <div className="grid max-w-2xl grid-cols-1 gap-x-6 gap-y-8 sm:grid-cols-6">
            {children}
          </div>
        </div>
      </div>
    </div>
  );
}

export function FormActions({
  children,
  className,
}: {
  children: React.ReactNode;
  className?: string;
}) {
  return (
    <div
      className={cn(
        "mt-6 flex items-center justify-end gap-x-4 border-t border-border px-4 py-4 sm:px-8",
        className
      )}
    >
      {children}
    </div>
  );
}
