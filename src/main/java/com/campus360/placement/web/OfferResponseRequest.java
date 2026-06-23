package com.campus360.placement.web;

import jakarta.validation.constraints.NotBlank;

/** decision is ACCEPT or DECLINE. */
public record OfferResponseRequest(@NotBlank String decision) {
}
