import { apiRequest } from "./client";
import type {
  Application,
  Company,
  EligibilityCriteria,
  JobPosting,
  Offer,
  PlacementStats,
  StudentProfile,
} from "./entities";

// Companies
export const listCompanies = () => apiRequest<Company[]>("/api/v1/placements/companies");
export const createCompany = (body: {
  name: string;
  sector?: string;
  tier?: string;
  website?: string;
  description?: string;
}) => apiRequest<Company>("/api/v1/placements/companies", { method: "POST", body });

// Postings
export const listPostings = (openOnly = false) =>
  apiRequest<JobPosting[]>(`/api/v1/placements/postings?openOnly=${openOnly}`);
export const getPosting = (id: number) =>
  apiRequest<JobPosting>(`/api/v1/placements/postings/${id}`);
export const createPosting = (body: {
  companyId: number;
  title: string;
  type?: string;
  ctc?: number;
  location?: string;
  description?: string;
  eligibility?: EligibilityCriteria;
  closesAt?: string;
}) => apiRequest<JobPosting>("/api/v1/placements/postings", { method: "POST", body });

export const eligibleStudents = (postingId: number) =>
  apiRequest<StudentProfile[]>(`/api/v1/placements/postings/${postingId}/eligible-students`);

// Applications
export const applyToPosting = (postingId: number, studentId: number) =>
  apiRequest<Application>(`/api/v1/placements/postings/${postingId}/applications`, {
    method: "POST",
    body: { studentId },
  });

export const applicationsForPosting = (postingId: number) =>
  apiRequest<Application[]>(`/api/v1/placements/postings/${postingId}/applications`);

export const updateApplicationStatus = (id: number, status: string) =>
  apiRequest<Application>(`/api/v1/placements/applications/${id}/status`, {
    method: "PATCH",
    body: { status },
  });

export const myApplications = () =>
  apiRequest<Application[]>("/api/v1/placements/my/applications");

export const myOffers = () => apiRequest<Offer[]>("/api/v1/placements/my/offers");

// Offers
export const makeOffer = (applicationId: number, body: { ctc?: number; joiningDate?: string }) =>
  apiRequest<Offer>(`/api/v1/placements/applications/${applicationId}/offer`, {
    method: "POST",
    body,
  });

export const respondToOffer = (offerId: number, decision: "ACCEPT" | "DECLINE") =>
  apiRequest<Offer>(`/api/v1/placements/offers/${offerId}/respond`, {
    method: "POST",
    body: { decision },
  });

// Analytics
export const placementStats = () => apiRequest<PlacementStats>("/api/v1/placements/stats");

// Career Profile
import type { CareerProfile } from "./entities";
export const getCareerProfile = (studentId: number) =>
  apiRequest<CareerProfile>(`/api/v1/placements/career-profiles/${studentId}`);

export const updateCareerProfile = (studentId: number, body: Partial<CareerProfile>) =>
  apiRequest<CareerProfile>(`/api/v1/placements/career-profiles/${studentId}`, {
    method: "PUT",
    body,
  });

