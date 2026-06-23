package com.campus360.placement.web;

import jakarta.validation.constraints.NotBlank;

public record CompanyRequest(
        @NotBlank String name,
        String sector,
        String tier,
        String website,
        String description) {
}
