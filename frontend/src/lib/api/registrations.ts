import { apiRequest } from "./client";
import type { RegisterTenantRequest, RegistrationAck } from "./types";

export async function submitRegistration(
  req: RegisterTenantRequest,
): Promise<RegistrationAck> {
  return apiRequest<RegistrationAck>("/api/v1/registrations", {
    method: "POST",
    body: req,
    auth: false,
  });
}
