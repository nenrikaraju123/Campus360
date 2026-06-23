import React from "react";
import { Loader2 } from "lucide-react";

interface FullPageLoaderProps {
  message?: string;
}

export function FullPageLoader({ message = "Loading..." }: FullPageLoaderProps) {
  return (
    <div className="flex h-screen w-full flex-col items-center justify-center bg-gray-50/50 text-gray-500">
      <Loader2 className="h-8 w-8 animate-spin text-indigo-600 mb-4" />
      <p className="text-sm font-medium">{message}</p>
    </div>
  );
}
