import { apiRequest } from "./client";
import type { Institution, ProvisionResult, Registration, RegistrationStatus, TenantStats } from "./entities";

export function listRegistrations(status?: RegistrationStatus): Promise<Registration[]> {
  const q = status ? `?status=${status}` : "";
  return apiRequest<Registration[]>(`/api/v1/platform/registrations${q}`);
}

export function approveRegistration(id: number, notes?: string): Promise<ProvisionResult> {
  return apiRequest<ProvisionResult>(`/api/v1/platform/registrations/${id}/approve`, {
    method: "POST",
    body: { notes: notes ?? null },
  });
}

export function rejectRegistration(id: number, notes?: string): Promise<Registration> {
  return apiRequest<Registration>(`/api/v1/platform/registrations/${id}/reject`, {
    method: "POST",
    body: { notes: notes ?? null },
  });
}

export interface CreateInstitutionInput {
  institutionName: string;
  institutionCode: string;
  type?: string;
  adminFullName: string;
  adminEmail: string;
  password?: string;
}

export interface UpdateInstitutionInput {
  name?: string;
  type?: string;
  address?: string;
}

export function createInstitution(input: CreateInstitutionInput): Promise<ProvisionResult> {
  return apiRequest<ProvisionResult>("/api/v1/platform/institutions", {
    method: "POST",
    body: input,
  });
}

export function listInstitutions(): Promise<Institution[]> {
  return apiRequest<Institution[]>("/api/v1/platform/institutions");
}

export function updateInstitution(id: number, data: UpdateInstitutionInput): Promise<Institution> {
  return apiRequest<Institution>(`/api/v1/platform/institutions/${id}`, {
    method: "PUT",
    body: data,
  });
}

export function deactivateInstitution(id: number): Promise<Institution> {
  return apiRequest<Institution>(`/api/v1/platform/institutions/${id}`, { method: "DELETE" });
}

export function suspendInstitution(id: number): Promise<Institution> {
  return apiRequest<Institution>(`/api/v1/platform/institutions/${id}/suspend`, { method: "POST" });
}

export function activateInstitution(id: number): Promise<Institution> {
  return apiRequest<Institution>(`/api/v1/platform/institutions/${id}/activate`, { method: "POST" });
}

export function getTenantStats(id: number): Promise<TenantStats> {
  return apiRequest<TenantStats>(`/api/v1/platform/institutions/${id}/stats`);
}

export function getAllTenantStats(): Promise<TenantStats[]> {
  return apiRequest<TenantStats[]>("/api/v1/platform/institutions/stats");
}
