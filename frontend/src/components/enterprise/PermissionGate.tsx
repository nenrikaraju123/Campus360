import React from "react";
import { useAuthStore } from "@/lib/auth/store";
import { hasAnyRole } from "@/lib/auth/roles";
import type { Role } from "@/lib/api/types";

interface PermissionGateProps {
  requires: Role[];
  children: React.ReactNode;
  fallback?: React.ReactNode;
}

export function PermissionGate({ requires, children, fallback = null }: PermissionGateProps) {
  const user = useAuthStore((s) => s.user);

  if (!user || !hasAnyRole(user.roles, requires)) {
    return <>{fallback}</>;
  }

  return <>{children}</>;
}
