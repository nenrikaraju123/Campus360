import { create } from "zustand";
import type { LoginResponse, Role } from "@/lib/api/types";
import { decodeJwt } from "./jwt";

const REFRESH_KEY = "campus360.refreshToken";

export interface SessionUser {
  userId: number;
  tenantId: number | null;
  roles: Role[];
  email: string | null;
  mustChangePassword: boolean;
}

interface AuthState {
  accessToken: string | null;
  refreshToken: string | null;
  user: SessionUser | null;
  setSession: (res: LoginResponse) => void;
  clearMustChangePassword: () => void;
  clear: () => void;
}

export const useAuthStore = create<AuthState>((set) => ({
  accessToken: null,
  refreshToken: typeof localStorage !== "undefined" ? localStorage.getItem(REFRESH_KEY) : null,
  user: null,

  setSession: (res) => {
    localStorage.setItem(REFRESH_KEY, res.refreshToken);
    set({
      accessToken: res.accessToken,
      refreshToken: res.refreshToken,
      user: {
        userId: res.userId,
        tenantId: res.tenantId,
        roles: res.roles,
        email: decodeJwt(res.accessToken)?.email ?? null,
        mustChangePassword: res.mustChangePassword,
      },
    });
  },

  clearMustChangePassword: () =>
    set((s) => (s.user ? { user: { ...s.user, mustChangePassword: false } } : s)),

  clear: () => {
    localStorage.removeItem(REFRESH_KEY);
    set({ accessToken: null, refreshToken: null, user: null });
  },
}));

export const authActions = {
  getState: () => useAuthStore.getState(),
};
