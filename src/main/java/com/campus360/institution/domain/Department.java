package com.campus360.institution.domain;

import com.campus360.platform.persistence.AuditedEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "departments")
@Getter
@Setter
public class Department extends AuditedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(nullable = false, length = 160)
    private String name;

    @Column(nullable = false, length = 40)
    private String code;

    @Column(name = "hod_user_id")
    private Long hodUserId;
}
