package com.campus360.admissions.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "admission_offers")
public class AdmissionOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long tenantId;

    @Column(nullable = false, unique = true)
    private Long applicationId;

    @Column(nullable = false)
    private LocalDate offerDate;

    private LocalDate expiryDate;

    private Long programId;
    private Long departmentId;
    private Long sectionId;

    @Column(length = 20)
    private String academicYear;

    /** ISSUED, ACCEPTED, DECLINED, EXPIRED, CANCELLED */
    @Column(nullable = false, length = 30)
    private String status = "ISSUED";

    @Column(columnDefinition = "TEXT")
    private String conditions;

    private Long issuedBy;
    private Instant acceptedAt;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public Long getApplicationId() { return applicationId; }
    public void setApplicationId(Long applicationId) { this.applicationId = applicationId; }
    public LocalDate getOfferDate() { return offerDate; }
    public void setOfferDate(LocalDate offerDate) { this.offerDate = offerDate; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
    public Long getProgramId() { return programId; }
    public void setProgramId(Long programId) { this.programId = programId; }
    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
    public Long getSectionId() { return sectionId; }
    public void setSectionId(Long sectionId) { this.sectionId = sectionId; }
    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getConditions() { return conditions; }
    public void setConditions(String conditions) { this.conditions = conditions; }
    public Long getIssuedBy() { return issuedBy; }
    public void setIssuedBy(Long issuedBy) { this.issuedBy = issuedBy; }
    public Instant getAcceptedAt() { return acceptedAt; }
    public void setAcceptedAt(Instant acceptedAt) { this.acceptedAt = acceptedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
