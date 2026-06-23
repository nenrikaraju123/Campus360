import { apiRequest } from "./client";
import { ImportTemplate, ImportJob } from "./types";

export const importsApi = {
  getTemplate: (module: string, type: string) =>
    apiRequest<ImportTemplate>(`/api/v1/${module}/import-templates/${type}/download`),

  createImportJob: (module: string, file: File, type: string) => {
    // Note: Since we are uploading a file, we need a custom fetch instead of apiRequest
    // because apiRequest defaults to application/json.
    const formData = new FormData();
    formData.append("file", file);
    formData.append("type", type);
    
    // We'll wrap fetch here, or adjust apiRequest. For simplicity, wrapping fetch:
    const token = localStorage.getItem("auth-storage") ? JSON.parse(localStorage.getItem("auth-storage") as string)?.state?.accessToken : null;
    
    return fetch(`/api/v1/${module}/imports`, {
      method: "POST",
      headers: token ? { Authorization: `Bearer ${token}` } : {},
      body: formData,
    }).then(async (res) => {
      if (!res.ok) throw new Error("Import failed");
      return res.json() as Promise<ImportJob>;
    });
  },

  getJob: (module: string, jobId: number) =>
    apiRequest<ImportJob>(`/api/v1/${module}/imports/${jobId}`),

  validateJob: (module: string, jobId: number) =>
    apiRequest<void>(`/api/v1/${module}/imports/${jobId}/actions/validate`, { method: "POST" }),

  commitJob: (module: string, jobId: number) =>
    apiRequest<void>(`/api/v1/${module}/imports/${jobId}/actions/commit`, { method: "POST" }),
};
