package com.campus360.finance.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record RecordPaymentRequest(
        @NotNull Long invoiceId,
        @NotNull @Positive BigDecimal amount,
        String paymentMethod,
        String transactionRef) {
}
