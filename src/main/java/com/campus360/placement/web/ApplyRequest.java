package com.campus360.placement.web;

import jakarta.validation.constraints.NotNull;

public record ApplyRequest(@NotNull Long studentId) {
}
