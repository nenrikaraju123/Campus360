import { apiRequest } from "./client";
import type { StudentProfile } from "./entities";

// ── Basic Student CRUD (StudentController) ───────────────

export const listStudents = () => apiRequest<StudentProfile[]>("/api/v1/students");

export const getStudent = (id: number) => apiRequest<StudentProfile>(`/api/v1/students/${id}`);

export const getMyProfile = () => apiRequest<StudentProfile>("/api/v1/students/me");

export interface CreateStudentInput {
  fullName: string;
  email: string;
  password: string;
  rollNumber: string;
  branch?: string;
  batchYear?: number;
  programId?: number | null;
  admissionDate?: string;
}

export const createStudent = (body: CreateStudentInput) =>
  apiRequest<StudentProfile>("/api/v1/students", { method: "POST", body });

export const updateAcademics = (
  id: number,
  body: { cgpa?: number; activeBacklogs?: number; currentTerm?: number },
) => apiRequest<StudentProfile>(`/api/v1/students/${id}/academics`, { method: "PATCH", body });

// ── Student 360 (Student360Controller) ──────────────────

export interface StudentGuardian {
  id: number;
  tenantId: number;
  studentId: number;
  fullName: string;
  relationship: string;
  email?: string;
  phone?: string;
  occupation?: string;
  annualIncome?: number;
  isPrimary: boolean;
}

export interface StudentLifecycleHistory {
  id: number;
  studentId: number;
  eventType: string;
  fromStatus: string;
  toStatus: string;
  comment?: string;
  performedBy?: string;
  createdAt: string;
}

/** Full 360 profile */
export const getStudent360 = (id: number) =>
  apiRequest<StudentProfile>(`/api/v1/students/${id}/profile-360`);

/** Update personal info */
export const updatePersonal = (id: number, body: Record<string, string>) =>
  apiRequest<StudentProfile>(`/api/v1/students/${id}/personal`, { method: "PUT", body });

/** Update academic info */
export const updateAcademic = (id: number, body: Record<string, unknown>) =>
  apiRequest<StudentProfile>(`/api/v1/students/${id}/academic`, { method: "PUT", body });

// ── Guardians ────────────────────────────────────────────

export const listGuardians = (studentId: number) =>
  apiRequest<StudentGuardian[]>(`/api/v1/students/${studentId}/guardians`);

export const addGuardian = (studentId: number, data: Partial<StudentGuardian>) =>
  apiRequest<StudentGuardian>(`/api/v1/students/${studentId}/guardians`, {
    method: "POST",
    body: data,
  });

export const updateGuardian = (studentId: number, guardianId: number, data: Partial<StudentGuardian>) =>
  apiRequest<StudentGuardian>(`/api/v1/students/${studentId}/guardians/${guardianId}`, {
    method: "PUT",
    body: data,
  });

// ── Lifecycle Actions ────────────────────────────────────

export const getLifecycleHistory = (studentId: number) =>
  apiRequest<StudentLifecycleHistory[]>(`/api/v1/students/${studentId}/lifecycle-history`);

export const promoteStudent = (id: number, comment?: string) =>
  apiRequest<StudentProfile>(`/api/v1/students/${id}/actions/promote`, {
    method: "POST",
    body: { comment },
  });

export const suspendStudent = (id: number, comment?: string) =>
  apiRequest<StudentProfile>(`/api/v1/students/${id}/actions/suspend`, {
    method: "POST",
    body: { comment },
  });

export const graduateStudent = (id: number, comment?: string) =>
  apiRequest<StudentProfile>(`/api/v1/students/${id}/actions/graduate`, {
    method: "POST",
    body: { comment },
  });

export const archiveStudent = (id: number, comment?: string) =>
  apiRequest<StudentProfile>(`/api/v1/students/${id}/actions/archive`, {
    method: "POST",
    body: { comment },
  });

export const transferSection = (id: number, comment?: string) =>
  apiRequest<StudentProfile>(`/api/v1/students/${id}/actions/transfer-section`, {
    method: "POST",
    body: { comment },
  });

// ── Documents ────────────────────────────────────────────

export const listStudentDocuments = (studentId: number) =>
  apiRequest<any[]>(`/api/v1/students/${studentId}/documents`);

export const addStudentDocument = (studentId: number, data: Record<string, unknown>) =>
  apiRequest<any>(`/api/v1/students/${studentId}/documents`, { method: "POST", body: data });

// ── Bulk Import ──────────────────────────────────────────

export interface BulkStudentInput {
  fullName: string;
  email: string;
  rollNumber: string;
  branch?: string;
  batchYear?: number;
  programId?: number;
}

export interface BulkCreateStudentResult {
  email: string;
  rollNumber: string;
  success: boolean;
  student?: StudentProfile;
  error?: string;
}

export const bulkCreateStudents = (students: BulkStudentInput[]) =>
  apiRequest<BulkCreateStudentResult[]>("/api/v1/students/bulk", { method: "POST", body: students });

export const generateStudentImportTemplate = () =>
  apiRequest<string>("/api/v1/students/bulk-import/template", { method: "POST" });

export const startStudentBulkImport = (rows: Record<string, unknown>[]) =>
  apiRequest<{ jobId: string }>("/api/v1/students/bulk-import", { method: "POST", body: rows });

export const getStudentImportStatus = (jobId: string) =>
  apiRequest<{ status: string; validRows: number; invalidRows: number }>(
    `/api/v1/students/bulk-import/${jobId}`
  );

export const commitStudentImport = (jobId: string) =>
  apiRequest<{ status: string }>(`/api/v1/students/bulk-import/${jobId}/actions/commit`, {
    method: "POST",
  });
