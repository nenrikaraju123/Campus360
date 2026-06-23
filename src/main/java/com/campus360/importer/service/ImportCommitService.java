package com.campus360.importer.service;

import com.campus360.importer.domain.ImportJobRow;
import org.springframework.stereotype.Service;

@Service
public class ImportCommitService {

    public void commitRow(Long tenantId, ImportJobRow row, ImportHandler handler) {
        // Here we would extract JSON data, pass to handler.commitRow
        // On success, we save an ImportJobCommit to avoid re-running it.
    }
}
