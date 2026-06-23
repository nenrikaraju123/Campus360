import { apiRequest } from "./client";
import type { PageResponse } from "./types";

export interface User {
  id: number;
  tenantId: number;
  email: string;
  fullName: string;
  status: "ACTIVE" | "SUSPENDED";
  roles: { id: number; name: string }[];
  mustChangePassword: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CreateUserInput {
  fullName: string;
  email: string;
  password?: string;
  roles?: string[];
}

export interface BulkCreateUserResult {
  email: string;
  success: boolean;
  user?: User;
  error?: string;
}

export interface BulkCreateUsersInput {
  users: CreateUserInput[];
}

export const usersApi = {
  /** List users in current tenant (paginated, optional role filter) */
  listUsers: (page = 0, size = 20, role?: string) => {
    const params = new URLSearchParams({ page: String(page), size: String(size) });
    if (role) params.set("role", role);
    return apiRequest<PageResponse<User>>(`/api/v1/users?${params}`);
  },

  /** Get a single user */
  getUser: (id: number) =>
    apiRequest<User>(`/api/v1/users/${id}`),

  /** Create a single user (auto-generates & emails temp password if omitted) */
  createUser: (data: CreateUserInput) =>
    apiRequest<User>("/api/v1/users", { method: "POST", body: data }),

  /**
   * Bulk create users — returns per-row results (HTTP 207).
   * Failures on individual rows do NOT block others.
   */
  bulkCreateUsers: (data: BulkCreateUsersInput) =>
    apiRequest<BulkCreateUserResult[]>("/api/v1/users/bulk", { method: "POST", body: data }),

  /** Update user profile */
  updateUser: (id: number, data: { fullName?: string; email?: string }) =>
    apiRequest<User>(`/api/v1/users/${id}`, { method: "PUT", body: data }),

  /** Replace user roles */
  assignRoles: (id: number, roles: string[]) =>
    apiRequest<User>(`/api/v1/users/${id}/roles`, { method: "PUT", body: { roles } }),

  /** Suspend a user */
  suspendUser: (id: number) =>
    apiRequest<User>(`/api/v1/users/${id}/suspend`, { method: "POST" }),

  /** Activate a suspended user */
  activateUser: (id: number) =>
    apiRequest<User>(`/api/v1/users/${id}/activate`, { method: "POST" }),
};
