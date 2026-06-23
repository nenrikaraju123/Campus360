import { Link } from "@tanstack/react-router";
import { cn } from "@/lib/utils";

export function Brand({ className }: { className?: string }) {
  return (
    <Link to="/" className={cn("group flex items-center gap-2.5", className)}>
      <span className="grid size-8 place-items-center rounded-md bg-accent text-accent-foreground font-mono text-sm font-bold shadow-sm transition-transform group-hover:scale-105">
        C
      </span>
      <span className="text-[15px] font-semibold tracking-tight">
        Campus<span className="text-accent">360</span>
      </span>
    </Link>
  );
}
