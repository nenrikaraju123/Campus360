import { apiRequest } from "./client";

export const invitationsApi = {
  resendInvitation: (id: number) =>
    apiRequest<void>(`/api/v1/iam/invitations/${id}/actions/resend`, { method: "POST" }),

  revokeInvitation: (id: number) =>
    apiRequest<void>(`/api/v1/iam/invitations/${id}/actions/revoke`, { method: "POST" }),

  resendWelcome: (userId: number) =>
    apiRequest<void>(`/api/v1/iam/users/${userId}/actions/resend-welcome`, { method: "POST" }),
};
