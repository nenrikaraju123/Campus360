package com.campus360.student.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.Instant;

@Entity
@Table(name = "student_tags", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"tenant_id", "name"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StudentTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "color_hex", length = 10)
    private String colorHex;

    @Column(name = "created_by", length = 120)
    private String createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}
