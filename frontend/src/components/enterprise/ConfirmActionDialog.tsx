import React, { useState } from "react";
import { AlertTriangle, Loader2 } from "lucide-react";
import { Dialog } from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";

interface ConfirmActionDialogProps {
  open: boolean;
  onClose: () => void;
  onConfirm: () => Promise<void> | void;
  title: string;
  description: string;
  confirmText?: string;
  cancelText?: string;
  destructive?: boolean;
}

export function ConfirmActionDialog({
  open,
  onClose,
  onConfirm,
  title,
  description,
  confirmText = "Confirm",
  cancelText = "Cancel",
  destructive = true,
}: ConfirmActionDialogProps) {
  const [isPending, setIsPending] = useState(false);

  const handleConfirm = async () => {
    setIsPending(true);
    try {
      await onConfirm();
      onClose();
    } finally {
      setIsPending(false);
    }
  };

  return (
    <Dialog open={open} onClose={isPending ? () => {} : onClose} title={title}>
      <div className="flex flex-col items-center text-center sm:flex-row sm:text-left">
        {destructive && (
          <div className="mx-auto flex h-12 w-12 shrink-0 items-center justify-center rounded-full bg-red-100 sm:mx-0 sm:h-10 sm:w-10 mb-4 sm:mb-0 sm:mr-4">
            <AlertTriangle className="h-6 w-6 text-red-600" aria-hidden="true" />
          </div>
        )}
        <div className="mt-2 sm:mt-0">
          <p className="text-sm text-muted-foreground">{description}</p>
        </div>
      </div>
      <div className="mt-6 flex flex-col-reverse gap-2 sm:flex-row sm:justify-end">
        <Button
          type="button"
          variant="outline"
          onClick={onClose}
          disabled={isPending}
          className="w-full sm:w-auto"
        >
          {cancelText}
        </Button>
        <Button
          type="button"
          variant={destructive ? "destructive" : "default"}
          onClick={handleConfirm}
          disabled={isPending}
          className="w-full sm:w-auto"
        >
          {isPending && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
          {confirmText}
        </Button>
      </div>
    </Dialog>
  );
}
