import React, { ReactNode } from "react";
import { PaginationControls, PaginationState } from "./PaginationControls";
import { DataState } from "@/components/common";

export interface ColumnDef<T> {
  header: string;
  accessorKey?: keyof T;
  cell?: (item: T) => ReactNode;
  className?: string;
}

interface EnterpriseDataTableProps<T> {
  data: T[];
  columns: ColumnDef<T>[];
  keyExtractor: (item: T) => string | number;
  isLoading?: boolean;
  error?: unknown;
  emptyTitle?: string;
  emptyBody?: string;
  pagination?: {
    state: PaginationState;
    totalPages: number;
    totalElements: number;
    onPageChange: (page: number) => void;
  };
  onRowClick?: (item: T) => void;
}

export function EnterpriseDataTable<T>({
  data,
  columns,
  keyExtractor,
  isLoading = false,
  error,
  emptyTitle,
  emptyBody,
  pagination,
  onRowClick,
}: EnterpriseDataTableProps<T>) {
  return (
    <div className="rounded-md border border-border bg-card">
      <DataState
        isLoading={isLoading}
        error={error}
        isEmpty={!isLoading && !error && data.length === 0}
        emptyTitle={emptyTitle}
        emptyBody={emptyBody}
      >
        <div className="overflow-x-auto">
          <table className="w-full text-sm text-left">
            <thead className="border-b border-border bg-muted/40 text-xs uppercase text-muted-foreground">
              <tr>
                {columns.map((col, idx) => (
                  <th key={idx} className={`px-4 py-3 font-semibold ${col.className || ""}`}>
                    {col.header}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y divide-border">
              {data.map((item) => (
                <tr
                  key={keyExtractor(item)}
                  onClick={() => onRowClick?.(item)}
                  className={`transition-colors hover:bg-muted/50 ${onRowClick ? "cursor-pointer" : ""}`}
                >
                  {columns.map((col, idx) => (
                    <td key={idx} className={`px-4 py-3 align-middle ${col.className || ""}`}>
                      {col.cell ? col.cell(item) : col.accessorKey ? String(item[col.accessorKey]) : null}
                    </td>
                  ))}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        {pagination && (
          <PaginationControls
            page={pagination.state.page}
            totalPages={pagination.totalPages}
            totalElements={pagination.totalElements}
            onPageChange={pagination.onPageChange}
          />
        )}
      </DataState>
    </div>
  );
}
