package com.campus360.analytics.web.dto;

import java.math.BigDecimal;
import java.util.List;

public record AtRiskStudent(
        Long studentId,
        String rollNumber,
        String branch,
        BigDecimal cgpa,
        int activeBacklogs,
        List<String> riskFactors) {
}
