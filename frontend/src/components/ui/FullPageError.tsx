import React from "react";
import { AlertCircle, RotateCcw } from "lucide-react";
import { Button } from "@/components/ui/button";

interface FullPageErrorProps {
  error: unknown;
  reset?: () => void;
}

export function FullPageError({ error, reset }: FullPageErrorProps) {
  // Try to safely extract error message
  const errDetail =
    error instanceof Error ? error.message : typeof error === "string" ? error : "An unexpected error occurred.";

  return (
    <div className="flex h-screen w-full flex-col items-center justify-center bg-gray-50 p-4">
      <div className="max-w-md text-center flex flex-col items-center space-y-4">
        <div className="bg-red-100 p-3 rounded-full">
          <AlertCircle className="h-8 w-8 text-red-600" />
        </div>
        <h1 className="text-xl font-semibold text-gray-900">Something went wrong</h1>
        <p className="text-sm text-gray-500">{errDetail}</p>
        
        {reset && (
          <Button onClick={reset} variant="outline" className="mt-4 flex items-center gap-2">
            <RotateCcw className="h-4 w-4" />
            Try Again
          </Button>
        )}
      </div>
    </div>
  );
}
