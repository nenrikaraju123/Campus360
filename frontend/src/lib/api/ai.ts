import { apiRequest } from "./client";
import type { JobFitReport, ReadinessReport } from "./entities";

export const readiness = (studentId: number) =>
  apiRequest<ReadinessReport>(`/api/v1/ai/students/${studentId}/readiness`);

export const resumeFeedback = (studentId: number, resumeText: string) =>
  apiRequest<{ feedback: string }>(`/api/v1/ai/students/${studentId}/resume-feedback`, {
    method: "POST",
    body: { resumeText },
  });

export const mockInterview = (studentId: number, role?: string) =>
  apiRequest<{ questions: string }>(
    `/api/v1/ai/students/${studentId}/mock-interview${role ? `?role=${encodeURIComponent(role)}` : ""}`,
  );

export const jobFit = (studentId: number, postingId: number) =>
  apiRequest<JobFitReport>(`/api/v1/ai/students/${studentId}/job-fit/${postingId}`);
