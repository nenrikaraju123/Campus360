export interface AuditFields {
  createdAt?: string;
  updatedAt?: string;
  createdBy?: string;
  updatedBy?: string;
}

export type RegistrationStatus = "PENDING" | "APPROVED" | "REJECTED";

export interface Registration {
  id: number;
  institutionName: string;
  institutionCode: string;
  type: string;
  adminFullName: string;
  adminEmail: string;
  contactPhone?: string | null;
  message?: string | null;
  status: RegistrationStatus;
  reviewNotes?: string | null;
  reviewedBy?: string | null;
  reviewedAt?: string | null;
  institutionId?: number | null;
  createdAt: string;
  updatedAt: string;
}

export interface Institution extends AuditFields {
  id: number;
  name: string;
  code: string;
  type: string;
  address?: string | null;
  status: "ACTIVE" | "SUSPENDED" | "DEACTIVATED";
  createdAt: string;
  updatedAt?: string;
}

export interface TenantStats {
  institutionId: number;
  name: string;
  code: string;
  type: string;
  address?: string | null;
  status: string;
  createdAt: string;
  totalUsers: number;
  studentCount: number;
  facultyCount: number;
  hodCount: number;
  placementOfficerCount: number;
  financeCount: number;
}

export interface ProvisionResult {
  institutionId: number;
  institutionCode: string;
  adminEmail: string;
  temporaryPassword: string | null;
  mustChangePassword: boolean;
}

export interface Department extends AuditFields {
  id: number;
  tenantId: number;
  name: string;
  code: string;
  hodUserId?: number | null;
}

export interface Program extends AuditFields {
  id: number;
  tenantId: number;
  departmentId: number;
  name: string;
  code: string;
  level: string;
  durationTerms: number;
  totalCredits: number;
}

export interface Course extends AuditFields {
  id: number;
  tenantId: number;
  departmentId: number;
  code: string;
  title: string;
  creditHours: number;
  type: string;
  description?: string | null;
}

export interface AcademicTerm extends AuditFields {
  id: number;
  tenantId: number;
  name: string;
  startDate?: string | null;
  endDate?: string | null;
  addDropEnd?: string | null;
  status: string;
}

export interface Section extends AuditFields {
  id: number;
  tenantId: number;
  courseId: number;
  termId: number;
  facultyUserId?: number | null;
  capacity: number;
  schedule?: string | null;
}

export interface StudentProfile extends AuditFields {
  id: number;
  tenantId: number;
  userId: number;
  programId?: number | null;
  rollNumber: string;
  branch?: string | null;
  batchYear?: number | null;
  admissionDate?: string | null;
  currentTerm: number;
  cgpa: number;
  activeBacklogs: number;
}

export interface Company extends AuditFields {
  id: number;
  tenantId: number;
  name: string;
  sector?: string | null;
  tier?: string | null;
  website?: string | null;
  description?: string | null;
}

export interface EligibilityCriteria {
  minCgpa?: number | null;
  branches?: string[] | null;
  maxBacklogs?: number | null;
  batchYear?: number | null;
}

export interface JobPosting extends AuditFields {
  id: number;
  tenantId: number;
  companyId: number;
  title: string;
  type: string;
  ctc?: number | null;
  location?: string | null;
  description?: string | null;
  eligibility?: string | null; // JSON string — parse with JSON.parse
  status: string;
  postedBy?: number | null;
  closesAt?: string | null;
}

export interface Application {
  id: number;
  tenantId: number;
  postingId: number;
  studentId: number;
  status: string;
  appliedAt: string;
  updatedAt: string;
}

export interface Offer {
  id: number;
  tenantId: number;
  applicationId: number;
  ctc?: number | null;
  joiningDate?: string | null;
  status: string;
  createdAt: string;
  updatedAt: string;
}

export interface PlacementStats {
  totalStudents: number;
  placedStudents: number;
  placementRatePct: number;
  highestCtc: number;
  averageCtc: number;
  openPostings: number;
  totalOffers: number;
}

export interface ReadinessReport {
  studentId: number;
  score: number;
  band: "STRONG" | "DEVELOPING" | "AT_RISK";
  factors: string[];
  coaching: string;
  aiLive: boolean;
}

export interface JobFitReport {
  studentId: number;
  postingId: number;
  eligible: boolean;
  eligibilityGaps: string[];
  explanation: string;
  aiLive: boolean;
}

export interface NotificationMessage {
  tenantId: number;
  type: string;
  title: string;
  message: string;
  at: string;
}

export interface CareerProfile extends AuditFields {
  studentId: number;
  resumeRef?: string | null;
  skills?: string | null;
  certifications?: string | null;
  projects?: string | null;
  readinessScore: number;
}

export interface CurriculumItem extends AuditFields {
  id: number;
  programId: number;
  courseId: number;
  termNumber: number;
  mandatory: boolean;
}

export interface InstitutionDashboard {
  totalStudents: number;
  totalDepartments: number;
  openPostings: number;
  pendingInvoices: number;
  openGrievances: number;
}

export interface AtRiskStudent {
  studentId: number;
  studentName: string;
  rollNumber: string;
  cgpa: number;
  attendancePct: number;
  activeBacklogs: number;
  hasOverdueFees: boolean;
  riskReasons: string[];
}

export interface FeeStructure extends AuditFields {
  id: number;
  tenantId: number;
  programId: number;
  termId: number;
  name: string;
  amount: number;
  feeType: string;
  dueDate: string;
}

export interface Invoice {
  id: number;
  tenantId: number;
  studentId: number;
  feeStructureId: number;
  amount: number;
  paidAmount: number;
  invoiceNumber: string;
  dueDate: string;
  status: "PENDING" | "PARTIALLY_PAID" | "PAID" | "WAIVED";
  createdAt?: string;
  updatedAt?: string;
}

export interface Payment {
  id: number;
  tenantId: number;
  invoiceId: number;
  amount: number;
  paymentMethod: string;
  transactionRef: string;
  paidAt: string;
  recordedBy: string;
}

export interface StudentFeeSummary {
  studentId: number;
  totalDue: number;
  totalPaid: number;
  outstanding: number;
  pendingInvoices: number;
  overdueInvoices: number;
}

export interface Grievance extends AuditFields {
  id: number;
  studentId: number;
  category: "ACADEMIC" | "HOSTEL" | "FACILITY" | "FINANCE" | "OTHER";
  subject: string;
  description: string;
  priority: "LOW" | "MEDIUM" | "HIGH";
  status: "OPEN" | "IN_PROGRESS" | "RESOLVED" | "CLOSED";
  assignedTo?: number | null;
  resolution?: string | null;
}

export interface DocumentRequest extends AuditFields {
  id: number;
  studentId: number;
  type: "TRANSCRIPT" | "BONAFIDE" | "LEAVING_CERTIFICATE" | "OTHER";
  purpose: string;
  status: "PENDING" | "PROCESSING" | "READY" | "COLLECTED";
}

export interface AppNotification {
  id: number;
  userId: number;
  title: string;
  message: string;
  type: string;
  isRead: boolean;
  createdAt: string;
}

