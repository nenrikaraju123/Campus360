package com.campus360.placement.web;

public record CareerProfileRequest(
        String resumeRef,
        String skills,
        String certifications,
        String projects) {
}
