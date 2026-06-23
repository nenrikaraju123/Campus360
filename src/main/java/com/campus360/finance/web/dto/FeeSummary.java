package com.campus360.finance.web.dto;

import java.math.BigDecimal;

public record FeeSummary(
        Long studentId,
        BigDecimal totalDue,
        BigDecimal totalPaid,
        BigDecimal outstanding,
        long pendingInvoices,
        long overdueInvoices) {
}
