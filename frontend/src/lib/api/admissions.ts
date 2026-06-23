import { apiRequest } from "./client";

export const admissionsApi = {
  listLeads: () =>
    apiRequest<any[]>(`/api/v1/admissions/leads`),

  createLead: (data: any) =>
    apiRequest<any>(`/api/v1/admissions/leads`, { method: "POST", body: data }),

  listApplications: () =>
    apiRequest<any[]>(`/api/v1/admissions/applications`),

  createApplication: (data: any) =>
    apiRequest<any>(`/api/v1/admissions/applications`, { method: "POST", body: data }),

  getApplication: (id: number) =>
    apiRequest<any>(`/api/v1/admissions/applications/${id}`),

  updateApplication: (id: number, data: any) =>
    apiRequest<any>(`/api/v1/admissions/applications/${id}`, { method: "PUT", body: data }),

  addNote: (id: number, content: string) =>
    apiRequest<any>(`/api/v1/admissions/applications/${id}/notes`, { method: "POST", body: { content } }),

  submitReview: (id: number) =>
    apiRequest<void>(`/api/v1/admissions/applications/${id}/actions/submit-review`, { method: "POST" }),

  shortlist: (id: number) =>
    apiRequest<void>(`/api/v1/admissions/applications/${id}/actions/shortlist`, { method: "POST" }),

  approve: (id: number) =>
    apiRequest<void>(`/api/v1/admissions/applications/${id}/actions/approve`, { method: "POST" }),

  reject: (id: number) =>
    apiRequest<void>(`/api/v1/admissions/applications/${id}/actions/reject`, { method: "POST" }),

  waitlist: (id: number) =>
    apiRequest<void>(`/api/v1/admissions/applications/${id}/actions/waitlist`, { method: "POST" }),

  createOffer: (id: number, data: any) =>
    apiRequest<any>(`/api/v1/admissions/applications/${id}/actions/create-offer`, { method: "POST", body: data }),

  acceptOffer: (id: number) =>
    apiRequest<void>(`/api/v1/admissions/offers/${id}/actions/accept`, { method: "POST" }),

  enroll: (id: number) =>
    apiRequest<void>(`/api/v1/admissions/applications/${id}/actions/enroll`, { method: "POST" }),
};
