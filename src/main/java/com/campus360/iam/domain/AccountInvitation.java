package com.campus360.iam.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.Instant;

@Entity
@Table(name = "account_invitations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccountInvitation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(nullable = false, length = 180)
    private String email;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Column(nullable = false, length = 100, unique = true)
    private String token;

    /** PENDING, ACCEPTED, REVOKED, EXPIRED */
    @Column(nullable = false, length = 30)
    private String status = "PENDING";

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "created_by", length = 120)
    private String createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
    
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();
}
