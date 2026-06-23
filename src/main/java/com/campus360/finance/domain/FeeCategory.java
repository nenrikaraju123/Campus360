package com.campus360.finance.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.Instant;

@Entity
@Table(name = "fee_categories", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"tenant_id", "code"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FeeCategory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @Column(name = "created_by", length = 120)
    private String createdBy;

    @Column(name = "updated_by", length = 120)
    private String updatedBy;

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }
}
