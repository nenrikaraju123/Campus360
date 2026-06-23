export const ROLES = [
  "SUPER_ADMIN",
  "INSTITUTION_ADMIN",
  "HOD",
  "FACULTY",
  "STUDENT",
  "PLACEMENT_OFFICER",
  "RECRUITER",
  "PARENT",
  "ALUMNI",
] as const;

export type Role = (typeof ROLES)[number];

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresInSeconds: number;
  userId: number;
  tenantId: number | null;
  roles: Role[];
  mustChangePassword: boolean;
}

export interface ProblemDetails {
  type?: string;
  title?: string;
  status?: number;
  detail?: string;
  timestamp?: string;
}

export interface RegisterTenantRequest {
  institutionName: string;
  institutionCode: string;
  type?: string;
  adminFullName: string;
  adminEmail: string;
  contactPhone?: string;
  message?: string;
}

export interface RegistrationAck {
  id: number;
  status: "PENDING" | "APPROVED" | "REJECTED";
  message: string;
}

// Enterprise Modules

export interface TenantUsageSnapshot {
  id: number;
  tenantId: number;
  snapshotDate: string;
  totalUsers: number;
  storageBytes: number;
  apiCalls: number;
}

export interface ImportTemplate {
  id: number;
  tenantId: number;
  importType: string;
  templateVersion: string;
  columnsConfig: string; 
  active: boolean;
}

export interface ImportJob {
  id: number;
  tenantId: number;
  type: string;
  originalFileName: string;
  storageDocumentId?: string;
  status: "UPLOADED" | "PARSING" | "VALIDATING" | "VALIDATION_FAILED" | "READY_TO_COMMIT" | "COMMITTING" | "COMMITTED" | "PARTIALLY_COMMITTED" | "FAILED" | "CANCELLED";
  totalRows: number;
  validRows: number;
  invalidRows: number;
  committedRows: number;
  failedRows: number;
  uploadedBy?: string;
  committedBy?: string;
  createdAt: string;
}

export interface AccountInvitation {
  id: number;
  tenantId: number;
  email: string;
  token: string;
  status: "PENDING" | "ACCEPTED" | "REVOKED" | "EXPIRED";
  expiresAt: string;
  createdBy?: string;
  createdAt: string;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}
