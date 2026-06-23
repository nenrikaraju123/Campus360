package com.campus360.placement.domain;

import com.campus360.platform.persistence.AuditedEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "companies")
@Getter
@Setter
public class Company extends AuditedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 80)
    private String sector;

    /** DREAM | TIER1 | TIER2 | MASS — drives one-offer policy decisions. */
    @Column(length = 20)
    private String tier;

    @Column(length = 200)
    private String website;

    @Column(length = 2000)
    private String description;
}
