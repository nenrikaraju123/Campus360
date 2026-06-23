import { apiRequest } from "./client";

export const facultyApi = {
  listFaculty: (page: number = 0, size: number = 20) =>
    apiRequest<any>(`/api/v1/faculty?page=${page}&size=${size}`),

  createFaculty: (data: any) =>
    apiRequest<any>(`/api/v1/faculty`, { method: "POST", body: data }),

  getFaculty: (id: number) =>
    apiRequest<any>(`/api/v1/faculty/${id}`),

  updateFaculty: (id: number, data: any) =>
    apiRequest<any>(`/api/v1/faculty/${id}`, { method: "PUT", body: data }),

  assignCourse: (id: number, data: any) =>
    apiRequest<any>(`/api/v1/faculty/${id}/course-assignments`, { method: "POST", body: data }),

  myCourses: () =>
    apiRequest<any[]>(`/api/v1/faculty/me/courses`),

  generateImportTemplate: () =>
    apiRequest<void>(`/api/v1/faculty/bulk-import/template`, { method: "POST" }),

  uploadBulkImport: (data: any) =>
    apiRequest<any>(`/api/v1/faculty/bulk-import`, { method: "POST", body: data }),

  getBulkImportJob: (jobId: number) =>
    apiRequest<any>(`/api/v1/faculty/bulk-import/${jobId}`),

  commitBulkImport: (jobId: number) =>
    apiRequest<void>(`/api/v1/faculty/bulk-import/${jobId}/actions/commit`, { method: "POST" }),
};
