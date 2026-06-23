package com.campus360.placement.web;

import com.campus360.placement.domain.*;
import com.campus360.placement.service.PlacementService;
import com.campus360.placement.service.PlacementStats;
import com.campus360.platform.security.CurrentUser;
import com.campus360.student.domain.StudentProfile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/placements")
@Tag(name = "Placement & Career", description = "Companies, postings, eligibility, applications, offers")
public class PlacementController {

    private final PlacementService service;

    public PlacementController(PlacementService service) {
        this.service = service;
    }

    // Companies
    @PostMapping("/companies")
    @PreAuthorize("hasAnyRole('PLACEMENT_OFFICER','INSTITUTION_ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public Company createCompany(@Valid @RequestBody CompanyRequest req) {
        return service.createCompany(req);
    }

    @GetMapping("/companies")
    public List<Company> companies() {
        return service.listCompanies();
    }

    // Postings
    @PostMapping("/postings")
    @PreAuthorize("hasAnyRole('PLACEMENT_OFFICER','INSTITUTION_ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public JobPosting createPosting(@Valid @RequestBody PostingRequest req) {
        return service.createPosting(req, CurrentUser.id());
    }

    @GetMapping("/postings")
    public List<JobPosting> postings(@RequestParam(defaultValue = "true") boolean openOnly) {
        return service.listPostings(openOnly);
    }

    @GetMapping("/postings/{id}")
    public JobPosting posting(@PathVariable Long id) {
        return service.getPosting(id);
    }

    @GetMapping("/postings/{id}/eligible-students")
    @Operation(summary = "Run the eligibility engine: students who can apply to this posting")
    @PreAuthorize("hasAnyRole('PLACEMENT_OFFICER','INSTITUTION_ADMIN')")
    public List<StudentProfile> eligibleStudents(@PathVariable Long id) {
        return service.eligibleStudents(id);
    }

    // Applications
    @PostMapping("/postings/{id}/applications")
    @PreAuthorize("hasAnyRole('STUDENT','PLACEMENT_OFFICER','INSTITUTION_ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public Application apply(@PathVariable Long id, @Valid @RequestBody ApplyRequest req) {
        return service.apply(id, req.studentId());
    }

    @GetMapping("/postings/{id}/applications")
    @PreAuthorize("hasAnyRole('PLACEMENT_OFFICER','INSTITUTION_ADMIN')")
    public List<Application> applications(@PathVariable Long id) {
        return service.applicationsForPosting(id);
    }

    @PatchMapping("/applications/{id}/status")
    @PreAuthorize("hasAnyRole('PLACEMENT_OFFICER','INSTITUTION_ADMIN')")
    public Application updateStatus(@PathVariable Long id, @Valid @RequestBody StatusUpdateRequest req) {
        return service.updateApplicationStatus(id, req.status());
    }

    // Student self-service
    @GetMapping("/my/applications")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Applications belonging to the current student")
    public List<Application> myApplications() {
        return service.myApplications();
    }

    @GetMapping("/my/offers")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Offers belonging to the current student")
    public List<Offer> myOffers() {
        return service.myOffers();
    }

    // Offers
    @PostMapping("/applications/{id}/offer")
    @PreAuthorize("hasAnyRole('PLACEMENT_OFFICER','INSTITUTION_ADMIN','RECRUITER')")
    @ResponseStatus(HttpStatus.CREATED)
    public Offer makeOffer(@PathVariable Long id, @Valid @RequestBody OfferRequest req) {
        return service.makeOffer(id, req);
    }

    @PostMapping("/offers/{id}/respond")
    @PreAuthorize("hasAnyRole('STUDENT','PLACEMENT_OFFICER','INSTITUTION_ADMIN')")
    public Offer respond(@PathVariable Long id, @Valid @RequestBody OfferResponseRequest req) {
        return service.respondToOffer(id, req.decision());
    }

    // Analytics
    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('PLACEMENT_OFFICER','INSTITUTION_ADMIN','HOD')")
    @Operation(summary = "Placement statistics: placed %, highest/average CTC, offers")
    public PlacementStats stats() {
        return service.stats();
    }

    // Career Profiles
    @GetMapping("/career-profiles/{studentId}")
    @PreAuthorize("hasAnyRole('STUDENT','PLACEMENT_OFFICER','INSTITUTION_ADMIN')")
    @Operation(summary = "Get a student's career profile")
    public CareerProfile careerProfile(@PathVariable Long studentId) {
        return service.getCareerProfile(studentId);
    }

    @PutMapping("/career-profiles/{studentId}")
    @PreAuthorize("hasAnyRole('STUDENT','PLACEMENT_OFFICER','INSTITUTION_ADMIN')")
    @Operation(summary = "Update a student's career profile")
    public CareerProfile updateCareerProfile(@PathVariable Long studentId, @Valid @RequestBody CareerProfileRequest req) {
        return service.updateCareerProfile(studentId, req.resumeRef(), req.skills(), req.certifications(), req.projects());
    }
}
