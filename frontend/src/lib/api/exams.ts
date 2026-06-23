import { apiRequest } from "./client";

export interface ExamCycle {
  id: number;
  tenantId: number;
  name: string;
  termCode: string;
  startDate: string;
  endDate: string;
}

export interface ExamSchedule {
  id: number;
  tenantId: number;
  cycleId: number;
  courseCode: string;
  examDate: string;
  startTime: string;
  endTime: string;
}

export const examsApi = {
  listCycles: () =>
    apiRequest<ExamCycle[]>(`/api/v1/exams/cycles`),

  createCycle: (data: Partial<ExamCycle>) =>
    apiRequest<ExamCycle>(`/api/v1/exams/cycles`, { method: "POST", body: data }),

  listSchedules: () =>
    apiRequest<ExamSchedule[]>(`/api/v1/exams/schedules`),

  createSchedule: (data: Partial<ExamSchedule>) =>
    apiRequest<ExamSchedule>(`/api/v1/exams/schedules`, { method: "POST", body: data }),

  getMarkSheets: () =>
    apiRequest<any[]>(`/api/v1/exams/mark-sheets`),

  createMarkSheet: (data: any) =>
    apiRequest<any>(`/api/v1/exams/mark-sheets`, { method: "POST", body: data }),

  updateMarks: (id: number, marks: any[]) =>
    apiRequest<any>(`/api/v1/exams/mark-sheets/${id}/marks`, { method: "PUT", body: marks }),

  submitMarkSheet: (id: number) =>
    apiRequest<void>(`/api/v1/exams/mark-sheets/${id}/actions/submit`, { method: "POST" }),

  approveMarkSheet: (id: number) =>
    apiRequest<void>(`/api/v1/exams/mark-sheets/${id}/actions/approve`, { method: "POST" }),

  publishResult: (id: number) =>
    apiRequest<void>(`/api/v1/exams/results/${id}/actions/publish`, { method: "POST" }),

  getGradeCard: (studentId: number) =>
    apiRequest<any>(`/api/v1/exams/students/${studentId}/grade-card`),

  getResultsReport: () =>
    apiRequest<any>(`/api/v1/exams/reports/results`),
};
