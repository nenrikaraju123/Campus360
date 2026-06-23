package com.campus360.exams.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "result_publications")
@Getter
@Setter
public class ResultPublication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "exam_cycle_id", nullable = false)
    private Long examCycleId;

    @Column(name = "program_id", nullable = false)
    private Long programId;

    @Column(name = "term_id")
    private Long termId;

    @Column(name = "published_at", nullable = false)
    private Instant publishedAt;

    @Column(name = "published_by", nullable = false)
    private Long publishedBy;

    @Column(length = 500)
    private String remarks;
}
