package com.campus360.placement.eligibility;

import java.util.List;

/** Outcome of evaluating a student against a posting's criteria. */
public record EligibilityResult(boolean eligible, List<String> reasons) {

    public static EligibilityResult ok() {
        return new EligibilityResult(true, List.of());
    }
}
