package com.campus360.iam.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Platform-wide role. The set of names is seeded in migration V1 and mirrored
 * by {@link RoleName}.
 */
@Entity
@Table(name = "roles")
@Getter
@Setter
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String name;

    @jakarta.persistence.ManyToMany(fetch = jakarta.persistence.FetchType.EAGER)
    @jakarta.persistence.JoinTable(
            name = "role_permissions",
            joinColumns = @jakarta.persistence.JoinColumn(name = "role_id"),
            inverseJoinColumns = @jakarta.persistence.JoinColumn(name = "permission_id")
    )
    private java.util.Set<Permission> permissions = new java.util.HashSet<>();
}
