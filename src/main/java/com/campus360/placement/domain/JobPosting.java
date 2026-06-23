package com.campus360.placement.domain;

import com.campus360.platform.persistence.AuditedEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "job_postings")
@Getter
@Setter
public class JobPosting extends AuditedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(nullable = false, length = 200)
    private String title;

    /** FULL_TIME | INTERNSHIP | PPO */
    @Column(nullable = false, length = 20)
    private String type = "FULL_TIME";

    @Column(precision = 12, scale = 2)
    private BigDecimal ctc;

    @Column(length = 160)
    private String location;

    @Column(length = 4000)
    private String description;

    /** Eligibility rules as JSON: {minCgpa, branches[], maxBacklogs, batchYear}. */
    @Column(columnDefinition = "text")
    private String eligibility;

    /** DRAFT | OPEN | CLOSED */
    @Column(nullable = false, length = 20)
    private String status = "OPEN";

    @Column(name = "posted_by")
    private Long postedBy;

    @Column(name = "closes_at")
    private Instant closesAt;

    @Version
    private int version;
}
