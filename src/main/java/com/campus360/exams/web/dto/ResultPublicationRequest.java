package com.campus360.exams.web.dto;

import jakarta.validation.constraints.NotNull;

public record ResultPublicationRequest(
        @NotNull Long programId,
        Long termId,
        String remarks
) {}
