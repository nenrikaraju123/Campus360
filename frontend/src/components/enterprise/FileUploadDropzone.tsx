import React, { useCallback, useState } from "react";
import { UploadCloud, File as FileIcon, X } from "lucide-react";
import { cn } from "@/lib/utils";

interface FileUploadDropzoneProps {
  onFileSelect: (file: File) => void;
  accept?: string;
  maxSizeMB?: number;
  className?: string;
}

export function FileUploadDropzone({
  onFileSelect,
  accept = ".csv,.xlsx",
  maxSizeMB = 5,
  className,
}: FileUploadDropzoneProps) {
  const [isDragging, setIsDragging] = useState(false);
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [error, setError] = useState<string | null>(null);

  const handleDrag = (e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    if (e.type === "dragenter" || e.type === "dragover") setIsDragging(true);
    else if (e.type === "dragleave") setIsDragging(false);
  };

  const validateAndSelectFile = (file: File) => {
    setError(null);
    if (file.size > maxSizeMB * 1024 * 1024) {
      setError(`File size exceeds ${maxSizeMB}MB limit.`);
      return;
    }
    // Basic accept check (could be expanded)
    const fileExt = "." + file.name.split(".").pop();
    if (accept && !accept.includes(fileExt) && !accept.includes(file.type)) {
      setError(`Invalid file type. Accepted: ${accept}`);
      return;
    }

    setSelectedFile(file);
    onFileSelect(file);
  };

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragging(false);
    if (e.dataTransfer.files && e.dataTransfer.files[0]) {
      validateAndSelectFile(e.dataTransfer.files[0]);
    }
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      validateAndSelectFile(e.target.files[0]);
    }
  };

  return (
    <div className={cn("w-full", className)}>
      {!selectedFile ? (
        <div
          onDragEnter={handleDrag}
          onDragOver={handleDrag}
          onDragLeave={handleDrag}
          onDrop={handleDrop}
          className={cn(
            "relative flex flex-col items-center justify-center rounded-lg border-2 border-dashed p-10 text-center transition-colors",
            isDragging ? "border-accent bg-accent/5" : "border-border bg-card hover:bg-muted/50"
          )}
        >
          <UploadCloud className="mb-4 h-10 w-10 text-muted-foreground" />
          <h3 className="text-sm font-semibold text-foreground">Click to upload or drag and drop</h3>
          <p className="mt-1 text-xs text-muted-foreground">
            {accept} (Max {maxSizeMB}MB)
          </p>
          <input
            type="file"
            accept={accept}
            onChange={handleChange}
            className="absolute inset-0 cursor-pointer opacity-0"
          />
        </div>
      ) : (
        <div className="flex items-center justify-between rounded-md border border-border bg-card p-4">
          <div className="flex items-center gap-3">
            <div className="rounded-full bg-accent/10 p-2 text-accent">
              <FileIcon className="h-5 w-5" />
            </div>
            <div className="flex flex-col text-left">
              <span className="text-sm font-medium text-foreground">{selectedFile.name}</span>
              <span className="text-xs text-muted-foreground">
                {(selectedFile.size / 1024).toFixed(1)} KB
              </span>
            </div>
          </div>
          <button
            type="button"
            onClick={() => setSelectedFile(null)}
            className="rounded-full p-2 text-muted-foreground hover:bg-muted hover:text-foreground"
          >
            <X className="h-4 w-4" />
          </button>
        </div>
      )}
      {error && <p className="mt-2 text-sm font-medium text-destructive">{error}</p>}
    </div>
  );
}
