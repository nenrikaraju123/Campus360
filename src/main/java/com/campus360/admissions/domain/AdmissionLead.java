package com.campus360.admissions.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "admission_leads")
public class AdmissionLead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long tenantId;

    @Column(nullable = false, length = 80)
    private String firstName;

    @Column(nullable = false, length = 80)
    private String lastName;

    @Column(length = 180)
    private String email;

    @Column(length = 30)
    private String phone;

    /** WEBSITE, WALK_IN, REFERRAL, CAMPAIGN */
    @Column(length = 50)
    private String source;

    @Column(length = 120)
    private String programInterest;

    /** NEW, CONTACTED, CONVERTED, DISQUALIFIED */
    @Column(nullable = false, length = 30)
    private String status = "NEW";

    private Long assignedTo;

    @Column(columnDefinition = "TEXT")
    private String notes;

    private Long createdBy;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getProgramInterest() { return programInterest; }
    public void setProgramInterest(String programInterest) { this.programInterest = programInterest; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getAssignedTo() { return assignedTo; }
    public void setAssignedTo(Long assignedTo) { this.assignedTo = assignedTo; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
