package com.campus360.student.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "parent_student_links",
       uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id", "parent_id", "student_id"}))
public class ParentStudentLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long tenantId;

    /** IAM user with PARENT role */
    @Column(nullable = false)
    private Long parentId;

    @Column(nullable = false)
    private Long studentId;

    @Column(nullable = false, length = 50)
    private String relationship = "GUARDIAN";

    @Column(nullable = false)
    private boolean isActive = true;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public String getRelationship() { return relationship; }
    public void setRelationship(String relationship) { this.relationship = relationship; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
