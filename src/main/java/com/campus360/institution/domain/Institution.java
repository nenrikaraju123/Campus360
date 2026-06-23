package com.campus360.institution.domain;

import com.campus360.platform.persistence.AuditedEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** Tenant root: one row per college/university on the platform. */
@Entity
@Table(name = "institutions")
@Getter
@Setter
public class Institution extends AuditedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, unique = true, length = 40)
    private String code;

    @Column(nullable = false, length = 40)
    private String type = "UNIVERSITY";

    @Column(length = 500)
    private String address;

    @Column(nullable = false, length = 20)
    private String status = "ACTIVE";

    @Version
    private int version;
}
