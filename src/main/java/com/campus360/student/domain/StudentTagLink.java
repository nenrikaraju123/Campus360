package com.campus360.student.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.Instant;

@Entity
@Table(name = "student_tag_links", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"tenant_id", "student_id", "tag_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StudentTagLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private StudentProfile student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false)
    private StudentTag tag;

    @Column(name = "linked_by", length = 120)
    private String linkedBy;

    @Column(name = "linked_at", nullable = false, updatable = false)
    private Instant linkedAt = Instant.now();
}
