import type { Role } from "@/lib/api/types";

export interface JwtClaims {
  sub: string;
  email?: string;
  tenantId?: number | null;
  roles?: Role[];
  iat?: number;
  exp?: number;
}

/** Decode a JWT payload without verifying (UI gating only — server is authoritative). */
export function decodeJwt(token: string): JwtClaims | null {
  try {
    const payload = token.split(".")[1];
    const json = atob(payload.replace(/-/g, "+").replace(/_/g, "/"));
    return JSON.parse(decodeURIComponent(escape(json)));
  } catch {
    return null;
  }
}

export function isExpired(token: string): boolean {
  const claims = decodeJwt(token);
  if (!claims?.exp) return false;
  return claims.exp * 1000 <= Date.now();
}
