package com.campus360.analytics.web.dto;

public record InstitutionDashboard(
        long totalStudents,
        long departments,
        long programs,
        long courses,
        long sections,
        long openPostings,
        long totalOffers,
        long pendingInvoices,
        long openGrievances) {
}
