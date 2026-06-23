import { apiRequest } from "./client";
import type { AcademicTerm, Course, Department, Program, Section } from "./entities";

// Departments
export const listDepartments = () => apiRequest<Department[]>("/api/v1/departments");
export const createDepartment = (body: { name: string; code: string; hodUserId?: number | null }) =>
  apiRequest<Department>("/api/v1/departments", { method: "POST", body });

// Programs
export const listPrograms = () => apiRequest<Program[]>("/api/v1/programs");
export const createProgram = (body: {
  departmentId: number;
  name: string;
  code: string;
  level?: string;
  durationTerms?: number;
  totalCredits?: number;
}) => apiRequest<Program>("/api/v1/programs", { method: "POST", body });

// Courses
export const listCourses = () => apiRequest<Course[]>("/api/v1/courses");
export const createCourse = (body: {
  departmentId: number;
  code: string;
  title: string;
  creditHours?: number;
  type?: string;
  description?: string;
}) => apiRequest<Course>("/api/v1/courses", { method: "POST", body });

// Terms
export const listTerms = () => apiRequest<AcademicTerm[]>("/api/v1/terms");
export const createTerm = (body: {
  name: string;
  startDate?: string;
  endDate?: string;
  addDropEnd?: string;
  status?: string;
}) => apiRequest<AcademicTerm>("/api/v1/terms", { method: "POST", body });

// Sections
export const listSections = (termId?: number) =>
  apiRequest<Section[]>(`/api/v1/sections${termId ? `?termId=${termId}` : ""}`);
export const createSection = (body: {
  courseId: number;
  termId: number;
  facultyUserId?: number | null;
  capacity?: number;
  schedule?: string;
}) => apiRequest<Section>("/api/v1/sections", { method: "POST", body });

// Curriculum Items
import type { CurriculumItem } from "./entities";
export const getCurriculumItems = (programId: number) =>
  apiRequest<CurriculumItem[]>(`/api/v1/curriculum-items?programId=${programId}`);

export const addCurriculumItem = (body: Partial<CurriculumItem>) =>
  apiRequest<CurriculumItem>("/api/v1/curriculum-items", { method: "POST", body });

export const deleteCurriculumItem = (id: number) =>
  apiRequest<void>(`/api/v1/curriculum-items/${id}`, { method: "DELETE" });
