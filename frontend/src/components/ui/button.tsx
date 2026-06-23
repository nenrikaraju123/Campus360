import * as React from "react";
import { cn } from "@/lib/utils";

const base =
  "inline-flex items-center justify-center gap-2 whitespace-nowrap rounded-md text-sm font-medium transition-all duration-150 ease-[cubic-bezier(0.16,1,0.3,1)] focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 focus-visible:ring-offset-background disabled:pointer-events-none disabled:opacity-50 active:scale-[0.98]";

const variants = {
  accent: "bg-accent text-accent-foreground hover:brightness-110 shadow-sm",
  default: "bg-foreground text-background hover:opacity-90",
  outline: "border border-border bg-transparent hover:bg-muted",
  ghost: "hover:bg-muted text-foreground",
  destructive: "bg-destructive text-destructive-foreground hover:brightness-110",
} as const;

const sizes = {
  sm: "h-8 px-3",
  md: "h-10 px-4",
  lg: "h-11 px-6 text-[15px]",
  icon: "h-9 w-9",
} as const;

export interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: keyof typeof variants;
  size?: keyof typeof sizes;
}

export const Button = React.forwardRef<HTMLButtonElement, ButtonProps>(
  ({ className, variant = "default", size = "md", ...props }, ref) => (
    <button ref={ref} className={cn(base, variants[variant], sizes[size], className)} {...props} />
  ),
);
Button.displayName = "Button";
