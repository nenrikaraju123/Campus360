package com.campus360.importer.service;

import com.campus360.importer.domain.ImportTemplate;
import com.campus360.importer.repository.ImportTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ImportTemplateService {

    private final ImportTemplateRepository repository;

    @Transactional(readOnly = true)
    public ImportTemplate getTemplate(Long tenantId, String type) {
        return repository.findByTenantIdAndType(tenantId, type)
                .orElseThrow(() -> new IllegalArgumentException("Template not found for type: " + type));
    }

    @Transactional
    public ImportTemplate createOrUpdateTemplate(Long tenantId, String type, String url, String columnsJson, String actorId) {
        ImportTemplate template = repository.findByTenantIdAndType(tenantId, type)
                .orElseGet(() -> {
                    ImportTemplate t = new ImportTemplate();
                    t.setTenantId(tenantId);
                    t.setType(type);
                    t.setCreatedBy(actorId);
                    return t;
                });
        template.setTemplateUrl(url);
        template.setColumnsJson(columnsJson);
        template.setUpdatedAt(java.time.Instant.now());
        return repository.save(template);
    }
}
