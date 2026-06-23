import { apiRequest } from "./client";
import type { PageResponse } from "./types";

export interface Enrollment {
  id: number;
  tenantId: number;
  sectionId: number;
  studentId: number;
  status: string; // ACTIVE, DROPPED, COMPLETED
  grade?: string;
  gradePoints?: number;
  enrolledAt: string;
}

export const enrollmentsApi = {
  /** Enroll a student in a section */
  enroll: (data: { studentId: number; sectionId: number }) =>
    apiRequest<Enrollment>("/api/v1/enrollments", { method: "POST", body: data }),

  /** List enrollments by section */
  getBySection: (sectionId: number) =>
    apiRequest<Enrollment[]>(`/api/v1/enrollments/by-section/${sectionId}`),

  /** List enrollments by student */
  getByStudent: (studentId: number) =>
    apiRequest<Enrollment[]>(`/api/v1/enrollments/by-student/${studentId}`),

  /** List enrollments by term */
  getByTerm: (termId: number) =>
    apiRequest<Enrollment[]>(`/api/v1/enrollments/by-term/${termId}`),

  /** Drop an enrollment */
  drop: (enrollmentId: number) =>
    apiRequest<Enrollment>(`/api/v1/enrollments/${enrollmentId}/drop`, { method: "POST" }),
};
