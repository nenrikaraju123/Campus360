import { apiRequest } from "./client";

export interface Room {
  id: number;
  tenantId: number;
  roomNumber: string;
  capacity: number;
  type: string; // LECTURE, LAB, SEMINAR
}

export interface TimeSlot {
  id: number;
  tenantId: number;
  dayOfWeek: string; // MONDAY...FRIDAY
  startTime: string; // HH:mm
  endTime: string;
  isBreak: boolean;
}

export interface TimetableTemplate {
  id: number;
  tenantId: number;
  termId: number;
  name: string;
  status: string; // DRAFT, PUBLISHED
}

export interface TimetableEntry {
  id: number;
  templateId: number;
  sectionId: number;
  timeSlotId: number;
  roomId?: number;
}

export interface TimetableConflict {
  type: string;
  description: string;
  entryIds: number[];
}

export const timetableApi = {
  // Rooms
  listRooms: () =>
    apiRequest<Room[]>("/api/v1/timetable/rooms"),

  createRoom: (data: Partial<Room>) =>
    apiRequest<Room>("/api/v1/timetable/rooms", { method: "POST", body: data }),

  // Time Slots
  listTimeSlots: () =>
    apiRequest<TimeSlot[]>("/api/v1/timetable/time-slots"),

  createTimeSlot: (data: Partial<TimeSlot>) =>
    apiRequest<TimeSlot>("/api/v1/timetable/time-slots", { method: "POST", body: data }),

  // Templates
  listTemplates: (termId?: number) =>
    apiRequest<TimetableTemplate[]>(`/api/v1/timetable/templates${termId ? `?termId=${termId}` : ""}`),

  createTemplate: (data: { termId: number; name: string }) =>
    apiRequest<TimetableTemplate>("/api/v1/timetable/templates", { method: "POST", body: data }),

  publishTemplate: (id: number) =>
    apiRequest<TimetableTemplate>(`/api/v1/timetable/templates/${id}/publish`, { method: "POST" }),

  getConflicts: (id: number) =>
    apiRequest<TimetableConflict[]>(`/api/v1/timetable/templates/${id}/conflicts`),

  // Entries
  addEntry: (templateId: number, data: { sectionId: number; timeSlotId: number; roomId?: number }) =>
    apiRequest<TimetableEntry>(`/api/v1/timetable/templates/${templateId}/entries`, {
      method: "POST",
      body: data,
    }),

  removeEntry: (templateId: number, entryId: number) =>
    apiRequest<void>(`/api/v1/timetable/templates/${templateId}/entries/${entryId}`, { method: "DELETE" }),

  getEntriesBySection: (templateId: number, sectionId: number) =>
    apiRequest<TimetableEntry[]>(
      `/api/v1/timetable/templates/${templateId}/entries?sectionId=${sectionId}`
    ),
};
