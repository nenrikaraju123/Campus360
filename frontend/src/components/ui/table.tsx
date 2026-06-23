import { motion } from "motion/react";
import { cn } from "@/lib/utils";

export function DataTable({
  columns,
  children,
  className,
}: {
  columns: string[];
  children: React.ReactNode;
  className?: string;
}) {
  return (
    <div className={cn("overflow-hidden rounded-lg border border-border bg-card", className)}>
      <div className="overflow-x-auto">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-border bg-muted/40">
              {columns.map((c) => (
                <th
                  key={c}
                  className="px-4 py-3 text-left text-[10px] font-semibold uppercase tracking-wider text-muted-foreground"
                >
                  {c}
                </th>
              ))}
            </tr>
          </thead>
          <tbody className="divide-y divide-border">{children}</tbody>
        </table>
      </div>
    </div>
  );
}

export function Row({ children, index = 0 }: { children: React.ReactNode; index?: number }) {
  return (
    <motion.tr
      initial={{ opacity: 0, y: 6 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.3, delay: Math.min(index * 0.04, 0.4), ease: [0.16, 1, 0.3, 1] }}
      className="transition-colors hover:bg-muted/30"
    >
      {children}
    </motion.tr>
  );
}

export function Cell({ children, className }: { children: React.ReactNode; className?: string }) {
  return <td className={cn("px-4 py-3 align-middle", className)}>{children}</td>;
}
