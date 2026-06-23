package com.campus360.faculty.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateFacultyRequest(
        @NotBlank @Size(max = 100) String firstName,
        @Size(max = 100) String lastName,
        @NotBlank @Email String email,
        @Size(max = 20) String phone,
        Long departmentId,
        @Size(max = 100) String designation,
        @Size(max = 200) String qualification,
        String employmentType,
        LocalDate joiningDate,
        java.util.List<String> roles
) {}
