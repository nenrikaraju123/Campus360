import { apiRequest } from "./client";
import type { FeeStructure, Invoice, Payment, StudentFeeSummary } from "./entities";
import type { PageResponse } from "./types";

// ── Fee Structures ──────────────────────────────────────

export const getFeeStructures = (programId?: number) =>
  apiRequest<FeeStructure[]>(`/api/v1/finance/fees${programId ? `?programId=${programId}` : ""}`);

export const createFeeStructure = (req: {
  programId?: number;
  termId?: number;
  name: string;
  amount: number;
  feeType?: string;
  dueDate?: string;
}) => apiRequest<FeeStructure>("/api/v1/finance/fees", { method: "POST", body: req });

// ── Invoices ────────────────────────────────────────────

export const getAllInvoices = (page = 0, size = 20, status?: string) => {
  const params = new URLSearchParams({ page: String(page), size: String(size) });
  if (status) params.set("status", status);
  return apiRequest<PageResponse<Invoice>>(`/api/v1/finance/invoices?${params}`);
};

export const getInvoicesForStudent = (studentId: number) =>
  apiRequest<Invoice[]>(`/api/v1/finance/invoices/by-student/${studentId}`);

export const generateInvoice = (req: {
  studentId: number;
  feeStructureId: number;
  dueDate?: string;
}) => apiRequest<Invoice>("/api/v1/finance/invoices", { method: "POST", body: req });

export const generateBulkInvoices = (feeStructureId: number) =>
  apiRequest<Invoice[]>(`/api/v1/finance/invoices/bulk/${feeStructureId}`, { method: "POST" });

export const waiveInvoice = (id: number) =>
  apiRequest<Invoice>(`/api/v1/finance/invoices/${id}/waive`, { method: "POST" });

export const issueInvoice = (id: number) =>
  apiRequest<Invoice>(`/api/v1/finance/invoices/${id}/actions/issue`, { method: "POST" });

// ── Payments ────────────────────────────────────────────

export const recordPayment = (req: {
  invoiceId: number;
  amount: number;
  paymentMethod?: string;
  transactionRef?: string;
}) => apiRequest<Payment>("/api/v1/finance/payments", { method: "POST", body: req });

export const getPaymentsForInvoice = (invoiceId: number) =>
  apiRequest<Payment[]>(`/api/v1/finance/payments/by-invoice/${invoiceId}`);

// ── Student Summary ─────────────────────────────────────

export const getFeeSummary = (studentId: number) =>
  apiRequest<StudentFeeSummary>(`/api/v1/finance/summary/${studentId}`);

// ── Fee Categories & Components (FeePlanController) ─────

export interface FeeCategory {
  id: number;
  tenantId: number;
  name: string;
  description?: string;
}

export interface FeeComponent {
  id: number;
  tenantId: number;
  categoryId: number;
  name: string;
  defaultAmount: number;
  isOptional: boolean;
}

export const getFeeCategories = () =>
  apiRequest<FeeCategory[]>("/api/v1/finance/fee-categories");

export const createFeeCategory = (data: Partial<FeeCategory>) =>
  apiRequest<FeeCategory>("/api/v1/finance/fee-categories", { method: "POST", body: data });

export const getFeeComponents = (categoryId: number) =>
  apiRequest<FeeComponent[]>(`/api/v1/finance/fee-components?categoryId=${categoryId}`);

export const createFeeComponent = (data: Partial<FeeComponent>) =>
  apiRequest<FeeComponent>("/api/v1/finance/fee-components", { method: "POST", body: data });

// ── Student Fee Assignments (StudentFinanceController) ──

export interface StudentFeeAssignment {
  id: number;
  tenantId: number;
  studentId: number;
  feeStructureId: number;
  dueDate?: string;
  note?: string;
}

export const getStudentFeeAssignments = (studentId: number) =>
  apiRequest<StudentFeeAssignment[]>(`/api/v1/finance/students/${studentId}/fee-assignments`);

export const assignFeeToStudent = (studentId: number, data: Partial<StudentFeeAssignment>) =>
  apiRequest<StudentFeeAssignment>(`/api/v1/finance/students/${studentId}/fee-assignments`, {
    method: "POST",
    body: data,
  });

export const getStudentLedger = (studentId: number) =>
  apiRequest<any[]>(`/api/v1/finance/students/${studentId}/ledger`);

// ── Finance Reports (FinanceReportController) ───────────

export interface FinanceCollectionSummary {
  [key: string]: unknown;
}

export const getCollectionSummary = () =>
  apiRequest<FinanceCollectionSummary>("/api/v1/finance/reports/collection-summary");

export const getDueAging = () =>
  apiRequest<Record<string, unknown>>("/api/v1/finance/reports/due-aging");

