package com.campus360.iam.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * Request body for bulk user creation.
 * Each entry is validated independently; the response reports per-row outcome.
 */
public record BulkCreateUsersRequest(
        @NotEmpty @Valid List<CreateUserRequest> users) {
}
