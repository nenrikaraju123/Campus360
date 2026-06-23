import { apiRequest } from "./client";

/**
 * IAM Account Invitation & Welcome notification management.
 * These endpoints are on /api/v1/iam/ and require PLATFORM_ADMIN or INSTITUTION_ADMIN.
 */
export const iamApi = {
  /** Resend an invitation email */
  resendInvitation: (invitationId: number) =>
    apiRequest<void>(`/api/v1/iam/invitations/${invitationId}/actions/resend`, { method: "POST" }),

  /** Revoke a pending invitation */
  revokeInvitation: (invitationId: number) =>
    apiRequest<void>(`/api/v1/iam/invitations/${invitationId}/actions/revoke`, { method: "POST" }),

  /** Resend a welcome notification to a user */
  resendWelcome: (userId: number) =>
    apiRequest<void>(`/api/v1/iam/users/${userId}/actions/resend-welcome`, { method: "POST" }),
};
