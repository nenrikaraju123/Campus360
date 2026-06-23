import { apiRequest } from "./client";
import type { Grievance, DocumentRequest } from "./entities";
import type { PageResponse } from "./types";

// ── Grievances ──────────────────────────────────────────

/** Submit a new grievance */
export const createGrievance = (req: Partial<Grievance>) =>
  apiRequest<Grievance>("/api/v1/student-life/grievances", { method: "POST", body: req });

/** My submitted grievances (student view) */
export const getMyGrievances = () =>
  apiRequest<Grievance[]>("/api/v1/student-life/grievances/my");

/** All grievances for admin/HOD (paginated) */
export const getAllGrievances = (page = 0, size = 20, status?: string) => {
  const params = new URLSearchParams({ page: String(page), size: String(size) });
  if (status) params.set("status", status);
  return apiRequest<PageResponse<Grievance>>(`/api/v1/student-life/grievances?${params}`);
};

/** Update grievance status (admin/HOD) */
export const updateGrievanceStatus = (id: number, status: string, resolution?: string) =>
  apiRequest<Grievance>(`/api/v1/student-life/grievances/${id}/status`, {
    method: "PATCH",
    body: { status, resolution },
  });

/** Assign grievance to a staff member */
export const assignGrievance = (id: number, assigneeId: number) =>
  apiRequest<Grievance>(`/api/v1/student-life/grievances/${id}/assign?assigneeId=${assigneeId}`, {
    method: "POST",
  });

// ── Leave Requests ──────────────────────────────────────

export interface LeaveRequest {
  id: number;
  studentId: number;
  fromDate: string;
  toDate: string;
  reason: string;
  status: "PENDING" | "APPROVED" | "REJECTED";
}

/** Submit a leave request */
export const createLeave = (req: Partial<LeaveRequest>) =>
  apiRequest<LeaveRequest>("/api/v1/student-life/leaves", { method: "POST", body: req });

/** Get leaves for a student */
export const getLeavesByStudent = (studentId: number) =>
  apiRequest<LeaveRequest[]>(`/api/v1/student-life/leaves/by-student/${studentId}`);

/** Get pending leaves for review */
export const getPendingLeaves = () =>
  apiRequest<LeaveRequest[]>("/api/v1/student-life/leaves/pending");

/** Approve or reject a leave */
export const reviewLeave = (id: number, decision: "APPROVED" | "REJECTED") =>
  apiRequest<LeaveRequest>(`/api/v1/student-life/leaves/${id}/review?decision=${decision}`, {
    method: "POST",
  });

// ── Document Requests ───────────────────────────────────

/** Request a document (transcript, bonafide, etc.) */
export const createDocumentRequest = (req: Partial<DocumentRequest>) =>
  apiRequest<DocumentRequest>("/api/v1/student-life/documents", { method: "POST", body: req });

/** Get document requests for a student */
export const getDocumentsByStudent = (studentId: number) =>
  apiRequest<DocumentRequest[]>(`/api/v1/student-life/documents/by-student/${studentId}`);

/** Get all pending document requests (admin/HOD) */
export const getPendingDocuments = () =>
  apiRequest<DocumentRequest[]>("/api/v1/student-life/documents/pending");

/** Update document request status */
export const updateDocumentStatus = (id: number, status: string) =>
  apiRequest<DocumentRequest>(`/api/v1/student-life/documents/${id}/status?status=${status}`, {
    method: "PATCH",
  });

// ── Backward-compatible aliases (for existing page components) ──

/** @deprecated Use getDocumentsByStudent(studentId) */
export const getMyDocumentRequests = () => getPendingDocuments();

/** @deprecated Use getAllGrievances() with no args for the paginated endpoint */
export const getAllDocumentRequests = () => getPendingDocuments();

