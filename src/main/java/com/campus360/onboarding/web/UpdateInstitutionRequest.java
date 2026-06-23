package com.campus360.onboarding.web;

import jakarta.validation.constraints.Size;

/**
 * Request body to update an institution's mutable profile details.
 * All fields are optional — only non-null values are applied.
 */
public record UpdateInstitutionRequest(
        @Size(max = 200) String name,
        @Size(max = 40) String type,
        @Size(max = 500) String address) {
}
