/**
 * Central API index — re-exports all frontend API modules.
 * Every backend controller must have a corresponding module here.
 */

// Auth
export * from "./auth";

// Platform / Onboarding
export * from "./platform";

// IAM
export * from "./users";
export * from "./iam";

// Institution Academics
export * from "./academics";
export { enrollmentsApi, type Enrollment } from "./enrollments";
export { gradesApi, type Assessment, type Mark, type TermResult, type EnrollmentResult } from "./grades";
export * from "./attendance";
export * from "./timetable";
export * from "./exams";

// Students
export * from "./students";

// Student Life
export * from "./studentlife";

// Finance
export * from "./finance";

// Admissions
export * from "./admissions";

// Placement
export * from "./placement";

// Faculty
export * from "./faculty";

// Analytics
export * from "./analytics";

// Notifications
export * from "./notifications";

// Parent Portal
export * from "./parent";

// Imports (generic module)
export * from "./imports";
