package com.campus360.student.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "student_lifecycle_history")
public class StudentLifecycleHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long tenantId;

    @Column(nullable = false)
    private Long studentId;

    @Column(length = 30)
    private String fromStatus;

    @Column(nullable = false, length = 30)
    private String toStatus;

    /** PROMOTED, SUSPENDED, GRADUATED, ARCHIVED, TRANSFERRED, WITHDRAWN */
    @Column(length = 50)
    private String action;

    @Column(columnDefinition = "TEXT")
    private String comment;

    private Long actorId;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public String getFromStatus() { return fromStatus; }
    public void setFromStatus(String fromStatus) { this.fromStatus = fromStatus; }
    public String getToStatus() { return toStatus; }
    public void setToStatus(String toStatus) { this.toStatus = toStatus; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public Long getActorId() { return actorId; }
    public void setActorId(Long actorId) { this.actorId = actorId; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
