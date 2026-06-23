import { apiRequest } from "./client";
import type { InstitutionDashboard, AtRiskStudent } from "./entities";

export const getDashboard = () => 
  apiRequest<InstitutionDashboard>("/api/v1/analytics/dashboard");

export const getAtRiskStudents = () =>
  apiRequest<AtRiskStudent[]>("/api/v1/analytics/at-risk");
