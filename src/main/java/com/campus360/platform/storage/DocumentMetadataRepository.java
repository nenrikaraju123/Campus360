package com.campus360.platform.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DocumentMetadataRepository extends JpaRepository<DocumentMetadata, String> {
    Optional<DocumentMetadata> findByIdAndTenantId(String id, Long tenantId);
}
