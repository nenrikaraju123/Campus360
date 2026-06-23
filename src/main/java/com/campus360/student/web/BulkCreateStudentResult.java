package com.campus360.student.web;

import com.campus360.student.domain.StudentProfile;

/**
 * Per-row result for bulk student import.
 * {@code success=true} and {@code student} populated on success;
 * {@code success=false} and {@code error} populated on failure.
 */
public record BulkCreateStudentResult(
        String email,
        String rollNumber,
        boolean success,
        StudentProfile student,
        String error) {

    public static BulkCreateStudentResult ok(String email, String rollNumber, StudentProfile student) {
        return new BulkCreateStudentResult(email, rollNumber, true, student, null);
    }

    public static BulkCreateStudentResult fail(String email, String rollNumber, String error) {
        return new BulkCreateStudentResult(email, rollNumber, false, null, error);
    }
}
