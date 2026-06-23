package com.campus360.placement.eligibility;

import com.campus360.student.domain.StudentProfile;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Evaluates a student against a posting's JSON eligibility criteria.
 * Returns not just a yes/no but the specific reasons for ineligibility, which
 * power the "what to improve" hints surfaced to students.
 */
@Component
public class EligibilityEngine {

    private final ObjectMapper objectMapper;

    public EligibilityEngine(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public EligibilityCriteria parse(String json) {
        if (json == null || json.isBlank()) {
            return new EligibilityCriteria(null, null, null, null);
        }
        try {
            return objectMapper.readValue(json, EligibilityCriteria.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid eligibility criteria JSON: " + e.getMessage());
        }
    }

    public EligibilityResult evaluate(EligibilityCriteria c, StudentProfile s) {
        List<String> reasons = new ArrayList<>();

        if (c.minCgpa() != null && s.getCgpa().compareTo(c.minCgpa()) < 0) {
            reasons.add("CGPA " + s.getCgpa() + " is below the required " + c.minCgpa());
        }
        if (c.maxBacklogs() != null && s.getActiveBacklogs() > c.maxBacklogs()) {
            reasons.add("Active backlogs " + s.getActiveBacklogs() + " exceed the allowed " + c.maxBacklogs());
        }
        if (c.branches() != null && !c.branches().isEmpty()) {
            boolean match = s.getBranch() != null && c.branches().stream()
                    .anyMatch(b -> b.equalsIgnoreCase(s.getBranch()));
            if (!match) {
                reasons.add("Branch '" + s.getBranch() + "' is not in the eligible branches " + c.branches());
            }
        }
        if (c.batchYear() != null && (s.getBatchYear() == null || !c.batchYear().equals(s.getBatchYear()))) {
            reasons.add("Batch year " + s.getBatchYear() + " does not match the required " + c.batchYear());
        }

        return new EligibilityResult(reasons.isEmpty(), reasons);
    }
}
