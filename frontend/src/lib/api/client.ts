import { useAuthStore } from "@/lib/auth/store";
import type { LoginResponse } from "./types";

export const API_BASE: string =
  (import.meta.env.VITE_API_BASE_URL as string | undefined) ?? "http://localhost:8080";

export class ApiError extends Error {
  status: number;
  code?: string;
  detail: string;
  fieldErrors?: { field: string; message: string; rejectedValue?: any }[];

  constructor(
    status: number,
    detail: string,
    code?: string,
    fieldErrors?: { field: string; message: string; rejectedValue?: any }[]
  ) {
    super(detail);
    this.name = "ApiError";
    this.status = status;
    this.detail = detail;
    this.code = code;
    this.fieldErrors = fieldErrors;
  }
}

async function toApiError(res: Response): Promise<ApiError> {
  let detail = res.statusText || "Request failed";
  let code: string | undefined = undefined;
  let fieldErrors: { field: string; message: string; rejectedValue?: any }[] | undefined = undefined;
  
  try {
    const body = await res.json();
    detail = body.message || body.detail || body.title || detail;
    code = body.code;
    fieldErrors = body.fieldErrors;
  } catch {
    /* non-JSON body */
  }
  return new ApiError(res.status, detail, code, fieldErrors);
}

// Single-flight refresh so concurrent 401s don't stampede the refresh endpoint.
let refreshInFlight: Promise<boolean> | null = null;

async function refreshSession(): Promise<boolean> {
  const { refreshToken, setSession, clear } = useAuthStore.getState();
  if (!refreshToken) return false;
  if (!refreshInFlight) {
    refreshInFlight = fetch(`${API_BASE}/api/v1/auth/refresh`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ refreshToken }),
    })
      .then(async (res) => {
        if (!res.ok) {
          clear();
          return false;
        }
        const data = (await res.json()) as LoginResponse;
        setSession(data);
        return true;
      })
      .catch(() => {
        clear();
        return false;
      })
      .finally(() => {
        refreshInFlight = null;
      });
  }
  return refreshInFlight;
}

interface RequestOptions {
  method?: string;
  body?: unknown;
  auth?: boolean; // default true
  _retried?: boolean;
}

export async function apiRequest<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const { method = "GET", body, auth = true } = options;
  const headers: Record<string, string> = { "Content-Type": "application/json" };

  if (auth) {
    const token = useAuthStore.getState().accessToken;
    if (token) headers.Authorization = `Bearer ${token}`;
  }

  const res = await fetch(`${API_BASE}${path}`, {
    method,
    headers,
    body: body !== undefined ? JSON.stringify(body) : undefined,
  });

  if (res.status === 401 && auth && !options._retried) {
    const refreshed = await refreshSession();
    if (refreshed) {
      return apiRequest<T>(path, { ...options, _retried: true });
    }
    useAuthStore.getState().clear();
    throw new ApiError(401, "Your session has expired. Please sign in again.");
  }

  if (!res.ok) {
    throw await toApiError(res);
  }

  if (res.status === 204) return undefined as T;
  return (await res.json()) as T;
}

/** On app start, exchange a persisted refresh token for a live session. */
export async function bootstrapAuth(): Promise<void> {
  const { accessToken, refreshToken } = useAuthStore.getState();
  if (!accessToken && refreshToken) {
    await refreshSession();
  }
}
