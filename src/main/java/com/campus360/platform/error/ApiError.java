package com.campus360.platform.error;

import java.time.Instant;
import java.util.List;

public class ApiError {
    private Instant timestamp;
    private int status;
    private String code;
    private String message;
    private String path;
    private String correlationId;
    private List<ValidationError> fieldErrors;

    public ApiError(int status, String code, String message, String path, String correlationId, List<ValidationError> fieldErrors) {
        this.timestamp = Instant.now();
        this.status = status;
        this.code = code;
        this.message = message;
        this.path = path;
        this.correlationId = correlationId;
        this.fieldErrors = fieldErrors;
    }

    public Instant getTimestamp() { return timestamp; }
    public int getStatus() { return status; }
    public String getCode() { return code; }
    public String getMessage() { return message; }
    public String getPath() { return path; }
    public String getCorrelationId() { return correlationId; }
    public List<ValidationError> getFieldErrors() { return fieldErrors; }
}
