package com.campus360.importer.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.Instant;

@Entity
@Table(name = "import_templates", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"tenant_id", "type"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ImportTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "type", nullable = false, length = 50)
    private String type; // STUDENTS, COURSES, FACULTY

    @Column(name = "template_url", nullable = false, length = 500)
    private String templateUrl;

    @Column(name = "columns_json", nullable = false, columnDefinition = "TEXT")
    private String columnsJson;

    @Column(name = "created_by", length = 120)
    private String createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
    
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();
}
