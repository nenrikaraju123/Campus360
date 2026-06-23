package com.campus360.iam.web.dto;

import com.campus360.iam.domain.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
        @Size(max = 160) String fullName,
        @Email String email,
        UserStatus status) {
}
