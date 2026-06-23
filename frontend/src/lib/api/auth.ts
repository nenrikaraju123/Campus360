import { apiRequest } from "./client";
import { useAuthStore } from "@/lib/auth/store";
import type { LoginResponse } from "./types";

export interface LoginParams {
  tenantCode?: string;
  email: string;
  password: string;
}

export async function login(params: LoginParams): Promise<LoginResponse> {
  const payload: Record<string, string> = { email: params.email, password: params.password };
  if (params.tenantCode && params.tenantCode.trim()) {
    payload.tenantCode = params.tenantCode.trim();
  }
  const res = await apiRequest<LoginResponse>("/api/v1/auth/login", {
    method: "POST",
    body: payload,
    auth: false,
  });
  useAuthStore.getState().setSession(res);
  return res;
}

export async function changePassword(currentPassword: string, newPassword: string): Promise<void> {
  await apiRequest<void>("/api/v1/auth/change-password", {
    method: "POST",
    body: { currentPassword, newPassword },
  });
  useAuthStore.getState().clearMustChangePassword();
}

export async function logout(): Promise<void> {
  const { refreshToken, clear } = useAuthStore.getState();
  try {
    if (refreshToken) {
      await apiRequest<void>("/api/v1/auth/logout", {
        method: "POST",
        body: { refreshToken },
        auth: false,
      });
    }
  } finally {
    clear();
  }
}
