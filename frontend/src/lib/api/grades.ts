import { apiRequest } from "./client";

export interface Assessment {
  id: number;
  tenantId: number;
  sectionId: number;
  title: string;
  type: string; // QUIZ, ASSIGNMENT, MIDTERM, FINAL, PROJECT
  maxMarks: number;
  weightage: number;
  conductedOn?: string;
}

export interface Mark {
  id: number;
  assessmentId: number;
  enrollmentId: number;
  marksObtained: number;
  remarks?: string;
}

export interface TermResult {
  id: number;
  studentId: number;
  termId: number;
  sgpa: number;
  cgpa: number;
  creditsEarned: number;
  totalCreditsAttempted: number;
}

export interface EnrollmentResult {
  id: number;
  tenantId: number;
  sectionId: number;
  studentId: number;
  status: string;
  grade?: string;
  gradePoints?: number;
}

/** @deprecated Use EnrollmentResult instead */
export type Enrollment = EnrollmentResult;

export const gradesApi = {
  /** Create an assessment for a section */
  createAssessment: (data: Partial<Assessment>) =>
    apiRequest<Assessment>("/api/v1/grades/assessments", { method: "POST", body: data }),

  /** List assessments for a section */
  getAssessmentsBySection: (sectionId: number) =>
    apiRequest<Assessment[]>(`/api/v1/grades/assessments/by-section/${sectionId}`),

  /** Get a single assessment */
  getAssessment: (id: number) =>
    apiRequest<Assessment>(`/api/v1/grades/assessments/${id}`),

  /** Bulk enter marks for an assessment */
  enterMarks: (data: { assessmentId: number; marks: { enrollmentId: number; marksObtained: number; remarks?: string }[] }) =>
    apiRequest<Mark[]>("/api/v1/grades/marks", { method: "POST", body: data }),

  /** Get marks for an assessment */
  getMarksByAssessment: (assessmentId: number) =>
    apiRequest<Mark[]>(`/api/v1/grades/marks/by-assessment/${assessmentId}`),

  /** Finalize grades and compute GPA for a section */
  finalizeGrades: (sectionId: number) =>
    apiRequest<EnrollmentResult[]>(`/api/v1/grades/finalize/${sectionId}`, { method: "POST" }),

  /** Manually recompute SGPA/CGPA for a student in a term */
  computeGpa: (studentId: number, termId: number) =>
    apiRequest<TermResult>(`/api/v1/grades/compute-gpa?studentId=${studentId}&termId=${termId}`, { method: "POST" }),

  /** Full transcript for a student */
  getTranscript: (studentId: number) =>
    apiRequest<TermResult[]>(`/api/v1/grades/transcript/${studentId}`),
};
