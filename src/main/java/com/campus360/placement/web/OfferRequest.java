package com.campus360.placement.web;

import java.math.BigDecimal;
import java.time.LocalDate;

public record OfferRequest(BigDecimal ctc, LocalDate joiningDate) {
}
