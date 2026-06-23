import React, { useState } from "react";
import { Download } from "lucide-react";
import { Button } from "@/components/ui/button";
import { FileUploadDropzone } from "./FileUploadDropzone";

interface ImportWizardProps {
  title: string;
  description: string;
  templateUrl?: string;
  onUpload: (file: File) => Promise<void>;
  // Can be extended to support multi-step Validation -> Error Review -> Commit
}

export function ImportWizard({ title, description, templateUrl, onUpload }: ImportWizardProps) {
  const [file, setFile] = useState<File | null>(null);
  const [isUploading, setIsUploading] = useState(false);

  const handleUpload = async () => {
    if (!file) return;
    setIsUploading(true);
    try {
      await onUpload(file);
    } finally {
      setIsUploading(false);
    }
  };

  return (
    <div className="mx-auto max-w-2xl rounded-xl border border-border bg-card shadow-sm">
      <div className="border-b border-border px-6 py-5">
        <h2 className="text-lg font-semibold text-foreground">{title}</h2>
        <p className="mt-1 text-sm text-muted-foreground">{description}</p>
      </div>
      <div className="p-6">
        {templateUrl && (
          <div className="mb-6 flex items-center justify-between rounded-lg bg-muted/50 p-4">
            <div>
              <h4 className="text-sm font-medium text-foreground">Step 1: Download Template</h4>
              <p className="text-xs text-muted-foreground">Use this template to format your data correctly.</p>
            </div>
            <a href={templateUrl} download className="inline-flex items-center justify-center whitespace-nowrap rounded-md text-sm font-medium transition-colors focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring disabled:pointer-events-none disabled:opacity-50 border border-input bg-background shadow-sm hover:bg-accent hover:text-accent-foreground h-8 rounded-md px-3 text-xs">
              <Download className="mr-2 h-4 w-4" />
              Template
            </a>
          </div>
        )}

        <div className="mb-6">
          <h4 className="mb-2 text-sm font-medium text-foreground">
            {templateUrl ? "Step 2: Upload File" : "Upload File"}
          </h4>
          <FileUploadDropzone onFileSelect={setFile} accept=".csv,.xlsx" />
        </div>

        <div className="flex justify-end border-t border-border pt-6">
          <Button onClick={handleUpload} disabled={!file || isUploading}>
            {isUploading ? "Uploading..." : "Next: Validate Data"}
          </Button>
        </div>
      </div>
    </div>
  );
}
