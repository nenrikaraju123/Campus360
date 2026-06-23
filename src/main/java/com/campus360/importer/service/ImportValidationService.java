package com.campus360.importer.service;

import com.campus360.importer.domain.ImportJobRow;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ImportValidationService {

    public boolean validateRow(Long tenantId, ImportJobRow row, ImportHandler handler) {
        // Here we would parse the JSON row data, pass it to the handler
        // If errors are returned by the handler, we create ImportJobError records.
        // Returning true for stub purposes.
        return true;
    }
}
