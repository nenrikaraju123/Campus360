import React from "react";
import { ChevronLeft, ChevronRight } from "lucide-react";
import { Button } from "@/components/ui/button";

export interface PaginationState {
  page: number; // 0-indexed
  size: number;
}

interface PaginationControlsProps {
  page: number; // 0-indexed
  totalPages: number;
  totalElements: number;
  onPageChange: (newPage: number) => void;
}

export function PaginationControls({
  page,
  totalPages,
  totalElements,
  onPageChange,
}: PaginationControlsProps) {
  if (totalPages <= 1) return null;

  return (
    <div className="flex items-center justify-between border-t border-border px-4 py-3 sm:px-6">
      <div className="hidden sm:flex sm:flex-1 sm:items-center sm:justify-between">
        <div>
          <p className="text-sm text-muted-foreground">
            Showing page <span className="font-medium text-foreground">{page + 1}</span> of{" "}
            <span className="font-medium text-foreground">{totalPages}</span>
            {" "} (<span className="font-medium text-foreground">{totalElements}</span> total items)
          </p>
        </div>
        <div>
          <nav className="isolate inline-flex -space-x-px rounded-md shadow-sm" aria-label="Pagination">
            <Button
              variant="outline"
              size="icon"
              className="rounded-l-md rounded-r-none border-r-0"
              onClick={() => onPageChange(page - 1)}
              disabled={page === 0}
            >
              <span className="sr-only">Previous</span>
              <ChevronLeft className="h-4 w-4" />
            </Button>
            
            <div className="flex items-center justify-center border border-border bg-background px-4 text-sm font-medium">
              {page + 1}
            </div>

            <Button
              variant="outline"
              size="icon"
              className="rounded-l-none rounded-r-md border-l-0"
              onClick={() => onPageChange(page + 1)}
              disabled={page >= totalPages - 1}
            >
              <span className="sr-only">Next</span>
              <ChevronRight className="h-4 w-4" />
            </Button>
          </nav>
        </div>
      </div>
      
      {/* Mobile view */}
      <div className="flex flex-1 justify-between sm:hidden">
        <Button
          variant="outline"
          onClick={() => onPageChange(page - 1)}
          disabled={page === 0}
        >
          Previous
        </Button>
        <div className="flex items-center text-sm">
          Page {page + 1} of {totalPages}
        </div>
        <Button
          variant="outline"
          onClick={() => onPageChange(page + 1)}
          disabled={page >= totalPages - 1}
        >
          Next
        </Button>
      </div>
    </div>
  );
}
