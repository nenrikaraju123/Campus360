package com.campus360.student.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.Instant;

@Entity
@Table(name = "student_documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StudentDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private StudentProfile student;

    @Column(name = "document_type", nullable = false, length = 100)
    private String documentType;

    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    @Column(name = "original_file_name", length = 255)
    private String originalFileName;

    @Column(name = "status", length = 50)
    private String status = "VERIFIED";

    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private Instant uploadedAt = Instant.now();

    @Column(name = "uploaded_by", length = 120)
    private String uploadedBy;

    @Column(name = "remarks", length = 500)
    private String remarks;
}
