package com.campus360.placement.web;

import jakarta.validation.constraints.NotBlank;

public record StatusUpdateRequest(@NotBlank String status) {
}
