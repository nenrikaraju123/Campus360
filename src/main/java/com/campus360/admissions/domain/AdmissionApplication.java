package com.campus360.admissions.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "admission_applications")
public class AdmissionApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long tenantId;

    private Long leadId;

    @Column(nullable = false, length = 80)
    private String firstName;

    @Column(nullable = false, length = 80)
    private String lastName;

    @Column(nullable = false, length = 180)
    private String email;

    @Column(length = 30)
    private String phone;

    private LocalDate dateOfBirth;

    @Column(length = 20)
    private String gender;

    @Column(length = 50)
    private String category;

    @Column(length = 50)
    private String quota;

    private Long programId;
    private Long departmentId;
    private Long preferredSectionId;

    @Column(length = 20)
    private String academicYear;

    @Column(length = 50)
    private String applicationNumber;

    /**
     * APPLICATION_RECEIVED → DOCUMENT_PENDING → UNDER_REVIEW → SHORTLISTED
     * → INTERVIEW_SCHEDULED → APPROVED → REJECTED | WAITLISTED
     * → OFFERED → ENROLLED → CANCELLED
     */
    @Column(nullable = false, length = 50)
    private String status = "APPLICATION_RECEIVED";

    private Long assignedReviewer;

    @Column(length = 120)
    private String guardianName;

    @Column(length = 180)
    private String guardianEmail;

    @Column(length = 30)
    private String guardianPhone;

    private Long createdBy;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    @Version
    private int version;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public Long getLeadId() { return leadId; }
    public void setLeadId(Long leadId) { this.leadId = leadId; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getQuota() { return quota; }
    public void setQuota(String quota) { this.quota = quota; }
    public Long getProgramId() { return programId; }
    public void setProgramId(Long programId) { this.programId = programId; }
    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
    public Long getPreferredSectionId() { return preferredSectionId; }
    public void setPreferredSectionId(Long preferredSectionId) { this.preferredSectionId = preferredSectionId; }
    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }
    public String getApplicationNumber() { return applicationNumber; }
    public void setApplicationNumber(String applicationNumber) { this.applicationNumber = applicationNumber; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getAssignedReviewer() { return assignedReviewer; }
    public void setAssignedReviewer(Long assignedReviewer) { this.assignedReviewer = assignedReviewer; }
    public String getGuardianName() { return guardianName; }
    public void setGuardianName(String guardianName) { this.guardianName = guardianName; }
    public String getGuardianEmail() { return guardianEmail; }
    public void setGuardianEmail(String guardianEmail) { this.guardianEmail = guardianEmail; }
    public String getGuardianPhone() { return guardianPhone; }
    public void setGuardianPhone(String guardianPhone) { this.guardianPhone = guardianPhone; }
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }
}
