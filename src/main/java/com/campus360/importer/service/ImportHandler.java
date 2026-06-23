package com.campus360.importer.service;

import java.util.Map;

public interface ImportHandler {

    String type();

    ImportValidationResult validateRow(Long tenantId, Map<String, String> row);

    ImportCommitResult commitRow(Long tenantId, Map<String, String> row);

    record ImportValidationResult(boolean isValid, String errorMessage) {}

    record ImportCommitResult(boolean success, String entityType, Long entityId, String errorMessage) {}
}
