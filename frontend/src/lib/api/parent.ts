import { apiRequest } from "./client";
import type { StudentProfile } from "./entities";

/**
 * Parent Portal API — all endpoints are scoped to the authenticated parent's
 * linked students. Only accessible to users with the PARENT role.
 */
export const parentApi = {
  /** List students linked to the authenticated parent */
  myStudents: () =>
    apiRequest<StudentProfile[]>("/api/v1/parents/me/students"),

  /** Get overview for a linked student */
  studentOverview: (studentId: number) =>
    apiRequest<StudentProfile>(`/api/v1/parents/me/students/${studentId}/overview`),

  /** Get attendance summary for a linked student */
  studentAttendance: (studentId: number) =>
    apiRequest<{ studentId: number; attendancePercentage: number }>(
      `/api/v1/parents/me/students/${studentId}/attendance`
    ),

  /** Get exam results for a linked student */
  studentResults: (studentId: number) =>
    apiRequest<any[]>(`/api/v1/parents/me/students/${studentId}/results`),

  /** Get fee ledger and invoices for a linked student */
  studentFees: (studentId: number) =>
    apiRequest<any[]>(`/api/v1/parents/me/students/${studentId}/fees`),
};
