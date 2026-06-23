package com.campus360.iam.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateUserRequest(
        @NotBlank @Size(max = 160) String fullName,
        @NotBlank @Email String email,
        @Size(min = 8, max = 72) String password,
        List<String> roles) {
}
