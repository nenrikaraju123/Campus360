import type { Role } from "@/lib/api/types";

const INSTITUTION_ROLES: Role[] = [
  "INSTITUTION_ADMIN",
  "HOD",
  "FACULTY",
  "PLACEMENT_OFFICER",
  "RECRUITER",
];

export function hasAnyRole(userRoles: Role[] | undefined, allowed: Role[]): boolean {
  if (!userRoles) return false;
  return userRoles.some((r) => allowed.includes(r));
}

/** Where a user lands after login, based on their highest-priority role. */
export function homeRouteForRoles(roles: Role[] | undefined): string {
  if (!roles) return "/login";
  if (roles.includes("SUPER_ADMIN")) return "/platform";
  if (roles.includes("STUDENT")) return "/student";
  if (hasAnyRole(roles, INSTITUTION_ROLES)) return "/institution";
  return "/login";
}

export { INSTITUTION_ROLES };
