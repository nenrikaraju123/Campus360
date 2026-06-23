package com.campus360.onboarding.web;

import jakarta.validation.constraints.Size;

/** Optional reviewer notes attached to an approve/reject decision. */
public record ReviewDecisionRequest(@Size(max = 1000) String notes) {
}
