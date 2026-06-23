import React, { Component, ReactNode } from "react";
import { AlertTriangle, Home, RefreshCw } from "lucide-react";

interface Props {
  children: ReactNode;
}

interface State {
  hasError: boolean;
  error: Error | null;
}

export class GlobalErrorBoundary extends Component<Props, State> {
  public state: State = {
    hasError: false,
    error: null,
  };

  public static getDerivedStateFromError(error: Error): State {
    return { hasError: true, error };
  }

  public componentDidCatch(error: Error, errorInfo: React.ErrorInfo) {
    console.error("Uncaught error:", error, errorInfo);
  }

  public render() {
    if (this.state.hasError) {
      return (
        <div className="min-h-screen bg-neutral-50 dark:bg-neutral-900 flex flex-col items-center justify-center p-4">
          <div className="max-w-md w-full bg-white dark:bg-neutral-800 p-8 rounded-lg shadow-sm border border-neutral-200 dark:border-neutral-700 text-center">
            <div className="w-16 h-16 bg-red-100 dark:bg-red-900/30 text-red-600 dark:text-red-400 rounded-full flex items-center justify-center mx-auto mb-6">
              <AlertTriangle size={32} />
            </div>
            <h1 className="text-2xl font-semibold text-neutral-900 dark:text-neutral-100 mb-2">
              Something went wrong
            </h1>
            <p className="text-neutral-500 dark:text-neutral-400 mb-8">
              We encountered an unexpected error. Our team has been notified.
            </p>
            
            <div className="flex flex-col sm:flex-row gap-3 justify-center">
              <button
                onClick={() => window.location.reload()}
                className="inline-flex items-center justify-center gap-2 px-4 py-2 bg-neutral-900 text-white dark:bg-white dark:text-neutral-900 rounded font-medium hover:bg-neutral-800 dark:hover:bg-neutral-100 transition-colors"
              >
                <RefreshCw size={18} />
                Try Again
              </button>
              <button
                onClick={() => window.location.href = "/"}
                className="inline-flex items-center justify-center gap-2 px-4 py-2 bg-white text-neutral-700 dark:bg-neutral-800 dark:text-neutral-300 border border-neutral-300 dark:border-neutral-600 rounded font-medium hover:bg-neutral-50 dark:hover:bg-neutral-700 transition-colors"
              >
                <Home size={18} />
                Go to Homepage
              </button>
            </div>
            
            {import.meta.env.DEV && this.state.error && (
              <div className="mt-8 text-left bg-neutral-100 dark:bg-neutral-900 p-4 rounded text-sm text-red-600 overflow-auto max-h-48 border border-neutral-200 dark:border-neutral-800">
                <p className="font-semibold mb-1">{this.state.error.toString()}</p>
                <pre className="text-xs whitespace-pre-wrap">{this.state.error.stack}</pre>
              </div>
            )}
          </div>
        </div>
      );
    }

    return this.props.children;
  }
}
