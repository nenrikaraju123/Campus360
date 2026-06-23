package com.campus360.student.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record CreateStudentRequest(
        @NotBlank String fullName,
        @NotBlank @Email String email,
        @NotBlank String password,
        @NotBlank String rollNumber,
        String branch,
        Integer batchYear,
        Long programId,
        LocalDate admissionDate) {
}
