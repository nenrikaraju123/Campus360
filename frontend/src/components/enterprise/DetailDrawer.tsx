import React, { useEffect } from "react";
import { motion, AnimatePresence } from "motion/react";
import { X } from "lucide-react";
import { Button } from "@/components/ui/button";

interface DetailDrawerProps {
  open: boolean;
  onClose: () => void;
  title: string;
  subtitle?: string;
  children: React.ReactNode;
  actions?: React.ReactNode;
}

export function DetailDrawer({ open, onClose, title, subtitle, children, actions }: DetailDrawerProps) {
  // Prevent scrolling on body when drawer is open
  useEffect(() => {
    if (open) {
      document.body.style.overflow = "hidden";
    } else {
      document.body.style.overflow = "unset";
    }
    return () => {
      document.body.style.overflow = "unset";
    };
  }, [open]);

  return (
    <AnimatePresence>
      {open && (
        <>
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            transition={{ duration: 0.2 }}
            className="fixed inset-0 z-40 bg-black/40 backdrop-blur-sm"
            onClick={onClose}
          />
          <motion.div
            initial={{ x: "100%" }}
            animate={{ x: 0 }}
            exit={{ x: "100%" }}
            transition={{ type: "spring", damping: 25, stiffness: 200 }}
            className="fixed inset-y-0 right-0 z-50 flex w-full max-w-md flex-col bg-card shadow-2xl sm:max-w-lg md:max-w-xl"
          >
            <div className="flex items-center justify-between border-b border-border px-6 py-4">
              <div>
                <h2 className="text-lg font-semibold text-foreground">{title}</h2>
                {subtitle && <p className="text-sm text-muted-foreground">{subtitle}</p>}
              </div>
              <Button variant="ghost" size="icon" onClick={onClose}>
                <X className="size-5" />
              </Button>
            </div>
            
            <div className="flex-1 overflow-y-auto px-6 py-6">
              {children}
            </div>

            {actions && (
              <div className="border-t border-border bg-muted/20 px-6 py-4 flex justify-end gap-2">
                {actions}
              </div>
            )}
          </motion.div>
        </>
      )}
    </AnimatePresence>
  );
}
