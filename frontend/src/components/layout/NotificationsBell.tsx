import { useEffect, useState } from "react";
import { motion, AnimatePresence } from "motion/react";
import { Bell, Briefcase, Radio, Check } from "lucide-react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { subscribeNotifications } from "@/lib/notifications";
import { getNotifications, markNotificationRead } from "@/lib/api/notifications";
import { shortDate } from "@/components/common";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { useAuthStore } from "@/lib/auth/store";

export function NotificationsBell() {
  const [open, setOpen] = useState(false);
  const queryClient = useQueryClient();
  const user = useAuthStore(s => s.user);

  const { data: notifications } = useQuery({
    queryKey: ["notifications"],
    queryFn: getNotifications,
    enabled: !!user,
    refetchInterval: 60000,
  });

  const markReadMutation = useMutation({
    mutationFn: markNotificationRead,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["notifications"] });
    }
  });

  useEffect(() => {
    const stop = subscribeNotifications((n) => {
      queryClient.invalidateQueries({ queryKey: ["notifications"] });
      toast(n.title, { description: n.message, icon: <Radio className="size-4 text-accent" /> });
    });
    return stop;
  }, [queryClient]);

  const items = notifications || [];
  const unread = items.filter(n => !n.isRead).length;

  return (
    <div className="relative">
      <Button variant="ghost" size="icon" onClick={() => setOpen(!open)} aria-label="Notifications">
        <Bell className="size-4" />
        {unread > 0 && (
          <span className="absolute -right-0.5 -top-0.5 grid size-4 place-items-center rounded-full bg-accent text-[9px] font-bold text-accent-foreground">
            {unread > 9 ? "9+" : unread}
          </span>
        )}
      </Button>

      <AnimatePresence>
        {open && (
          <>
            <div className="fixed inset-0 z-40" onClick={() => setOpen(false)} />
            <motion.div
              initial={{ opacity: 0, y: -6, scale: 0.98 }}
              animate={{ opacity: 1, y: 0, scale: 1 }}
              exit={{ opacity: 0, y: -6, scale: 0.98 }}
              transition={{ duration: 0.18, ease: [0.16, 1, 0.3, 1] }}
              className="absolute right-0 z-50 mt-2 w-80 overflow-hidden rounded-xl border border-border bg-card shadow-2xl"
            >
              <div className="flex items-center justify-between border-b border-border px-4 py-3">
                <span className="text-sm font-semibold">Notifications</span>
                <span className="font-mono text-[11px] text-muted-foreground">live & persistent</span>
              </div>
              <div className="max-h-80 overflow-y-auto">
                {items.length === 0 ? (
                  <p className="px-4 py-10 text-center text-sm text-muted-foreground">
                    No notifications yet.
                  </p>
                ) : (
                  items.map((n) => (
                    <div
                      key={n.id}
                      className={`flex gap-3 border-b border-border px-4 py-3 last:border-0 ${n.isRead ? 'opacity-70' : 'bg-muted/30'}`}
                    >
                      <div className="mt-0.5 grid size-8 shrink-0 place-items-center rounded-md bg-accent/10 text-accent">
                        <Briefcase className="size-4" />
                      </div>
                      <div className="min-w-0 flex-1">
                        <p className="text-sm font-medium">{n.title}</p>
                        <p className="text-xs text-muted-foreground">{n.message}</p>
                        <p className="mt-0.5 font-mono text-[10px] text-muted-foreground">
                          {shortDate(n.createdAt)}
                        </p>
                      </div>
                      {!n.isRead && (
                        <Button 
                          variant="ghost" 
                          size="icon" 
                          className="size-6 text-muted-foreground hover:text-success"
                          onClick={() => markReadMutation.mutate(n.id)}
                          title="Mark as read"
                        >
                          <Check className="size-3" />
                        </Button>
                      )}
                    </div>
                  ))
                )}
              </div>
            </motion.div>
          </>
        )}
      </AnimatePresence>
    </div>
  );
}
