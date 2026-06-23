package com.campus360.faculty.web.dto;

import com.campus360.faculty.domain.FacultyProfile;

public record BulkCreateFacultyResult(
        String email,
        String status, // "SUCCESS" or "ERROR"
        String employeeCode,
        Long facultyId,
        String errorReason
) {
    public static BulkCreateFacultyResult ok(String email, FacultyProfile profile) {
        return new BulkCreateFacultyResult(email, "SUCCESS", profile.getEmployeeCode(), profile.getId(), null);
    }

    public static BulkCreateFacultyResult fail(String email, String reason) {
        return new BulkCreateFacultyResult(email, "ERROR", null, null, reason);
    }
}
