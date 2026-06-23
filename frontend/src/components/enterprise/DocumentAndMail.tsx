import React from "react";
import { FileText, Download, Clock, CheckCircle2, XCircle, AlertCircle } from "lucide-react";
import { Button } from "@/components/ui/button";

interface DocumentItem {
  id: string | number;
  name: string;
  url?: string;
  sizeKb?: number;
  uploadedAt?: string;
}

export function DocumentList({ documents }: { documents: DocumentItem[] }) {
  if (documents.length === 0) {
    return <p className="text-sm text-muted-foreground">No documents uploaded.</p>;
  }

  return (
    <ul className="divide-y divide-border rounded-md border border-border">
      {documents.map((doc) => (
        <li key={doc.id} className="flex items-center justify-between py-3 pl-3 pr-4 text-sm">
          <div className="flex w-0 flex-1 items-center">
            <FileText className="h-5 w-5 shrink-0 text-muted-foreground" aria-hidden="true" />
            <div className="ml-4 flex min-w-0 flex-1 gap-2">
              <span className="truncate font-medium">{doc.name}</span>
              {doc.sizeKb && <span className="shrink-0 text-muted-foreground">{doc.sizeKb} KB</span>}
            </div>
          </div>
          <div className="ml-4 shrink-0">
            {doc.url ? (
              <a href={doc.url} download className="inline-flex items-center justify-center whitespace-nowrap rounded-md text-sm font-medium transition-colors focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring disabled:pointer-events-none disabled:opacity-50 hover:bg-accent hover:text-accent-foreground h-8 rounded-md px-3 text-accent">
                Download
              </a>
            ) : (
              <span className="text-xs text-muted-foreground">Unavailable</span>
            )}
          </div>
        </li>
      ))}
    </ul>
  );
}

interface MailStatus {
  status: "PENDING" | "SENT" | "FAILED" | "RETRYING";
  sentAt?: string;
  error?: string;
}

export function MailDeliveryStatus({ status, sentAt, error }: MailStatus) {
  switch (status) {
    case "SENT":
      return (
        <div className="flex items-center gap-2 text-sm text-green-600 dark:text-green-400">
          <CheckCircle2 className="h-4 w-4" />
          <span>Sent {sentAt ? `at ${sentAt}` : ""}</span>
        </div>
      );
    case "FAILED":
      return (
        <div className="flex flex-col gap-1">
          <div className="flex items-center gap-2 text-sm text-destructive">
            <XCircle className="h-4 w-4" />
            <span>Delivery Failed</span>
          </div>
          {error && <p className="text-xs text-muted-foreground">{error}</p>}
        </div>
      );
    case "PENDING":
    case "RETRYING":
      return (
        <div className="flex items-center gap-2 text-sm text-amber-600 dark:text-amber-400">
          <Clock className="h-4 w-4" />
          <span>{status === "RETRYING" ? "Retrying delivery..." : "Delivery pending..."}</span>
        </div>
      );
    default:
      return (
        <div className="flex items-center gap-2 text-sm text-muted-foreground">
          <AlertCircle className="h-4 w-4" />
          <span>Unknown status</span>
        </div>
      );
  }
}
