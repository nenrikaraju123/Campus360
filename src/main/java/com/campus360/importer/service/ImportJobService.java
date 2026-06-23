package com.campus360.importer.service;

import com.campus360.importer.domain.ImportJob;
import com.campus360.importer.repository.ImportJobRepository;
import com.campus360.platform.tenancy.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class ImportJobService {

    private final ImportJobRepository repository;
    private final Map<String, ImportHandler> handlers;

    public ImportJobService(ImportJobRepository repository, List<ImportHandler> handlerList) {
        this.repository = repository;
        this.handlers = handlerList.stream()
                .collect(Collectors.toMap(ImportHandler::type, h -> h));
    }

    public ImportJob createJob(String type, String originalFileName, String uploadedBy) {
        if (!handlers.containsKey(type)) {
            throw new IllegalArgumentException("Unsupported import type: " + type);
        }

        ImportJob job = new ImportJob();
        job.setTenantId(TenantContext.requireTenantId());
        job.setType(type);
        job.setOriginalFileName(originalFileName);
        job.setUploadedBy(uploadedBy);
        job.setStatus("UPLOADED");

        return repository.save(job);
    }
}
