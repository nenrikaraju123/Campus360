package com.campus360.student.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Single row in a bulk student import request.
 * Password is optional — if omitted, a secure temporary password is auto-generated
 * and emailed to the student.
 */
public record BulkStudentRequest(
        @NotBlank String fullName,
        @NotBlank @Email String email,
        @NotBlank String rollNumber,
        String branch,
        Integer batchYear,
        Long programId) {
}
