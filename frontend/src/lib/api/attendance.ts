import { apiRequest } from "./client";
import type { PageResponse } from "./types";

export interface AttendanceSummary {
  enrollmentId: number;
  studentId: number;
  sectionId: number;
  totalClasses: number;
  presentCount: number;
  attendancePercentage: number;
}

export interface ClassMeeting {
  id: number;
  tenantId: number;
  sectionId: number;
  meetingDate: string;
  startTime?: string;
  endTime?: string;
  topic?: string;
}

export interface AttendanceRecord {
  id: number;
  meetingId: number;
  enrollmentId: number;
  status: "PRESENT" | "ABSENT" | "LATE" | "EXCUSED";
}

export const attendanceApi = {
  /** Create a class meeting for a section */
  createMeeting: (data: Partial<ClassMeeting>) =>
    apiRequest<ClassMeeting>("/api/v1/attendance/meetings", { method: "POST", body: data }),

  /** List all meetings for a section */
  getMeetingsBySection: (sectionId: number) =>
    apiRequest<ClassMeeting[]>(`/api/v1/attendance/meetings/by-section/${sectionId}`),

  /** Bulk-mark attendance for a meeting */
  markBulk: (data: { meetingId: number; records: { enrollmentId: number; status: string }[] }) =>
    apiRequest<AttendanceRecord[]>("/api/v1/attendance/mark", { method: "POST", body: data }),

  /** Attendance summary for one enrollment */
  getSummary: (enrollmentId: number) =>
    apiRequest<AttendanceSummary>(`/api/v1/attendance/summary/${enrollmentId}`),

  /** Attendance summary for all students in a section */
  getSectionSummary: (sectionId: number) =>
    apiRequest<AttendanceSummary[]>(`/api/v1/attendance/summary/by-section/${sectionId}`),
};
