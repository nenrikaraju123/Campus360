package com.campus360.studentlife.web;

import com.campus360.shared.dto.PageResponse;
import com.campus360.studentlife.domain.*;
import com.campus360.studentlife.service.StudentLifeService;
import com.campus360.studentlife.web.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/student-life")
@Tag(name = "Student Life", description = "Grievances, leave requests, document requests")
public class StudentLifeController {

    private final StudentLifeService service;

    public StudentLifeController(StudentLifeService service) {
        this.service = service;
    }

    // ---- Grievances ----
    @PostMapping("/grievances")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Submit a grievance / support ticket")
    public Grievance createGrievance(@Valid @RequestBody GrievanceRequest req) {
        return service.createGrievance(req);
    }

    @GetMapping("/grievances")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN','HOD')")
    @Operation(summary = "List all grievances (optionally filter by status)")
    public PageResponse<Grievance> grievances(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status) {
        return service.listGrievances(page, size, status);
    }

    @GetMapping("/grievances/my")
    @Operation(summary = "My submitted grievances")
    public List<Grievance> myGrievances() {
        return service.myGrievances();
    }

    @PatchMapping("/grievances/{id}/status")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN','HOD')")
    public Grievance updateGrievanceStatus(@PathVariable Long id, @Valid @RequestBody StatusUpdateRequest req) {
        return service.updateGrievanceStatus(id, req.status(), req.resolution());
    }

    @PostMapping("/grievances/{id}/assign")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN','HOD')")
    @Operation(summary = "Assign a grievance to a staff member")
    public Grievance assignGrievance(@PathVariable Long id, @RequestParam Long assigneeId) {
        return service.assignGrievance(id, assigneeId);
    }

    // ---- Leave requests ----
    @PostMapping("/leaves")
    @PreAuthorize("hasAnyRole('STUDENT','INSTITUTION_ADMIN','HOD','FACULTY')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Submit a leave request")
    public LeaveRequest createLeave(@Valid @RequestBody LeaveRequestDto req) {
        return service.createLeaveRequest(req);
    }

    @GetMapping("/leaves/by-student/{studentId}")
    public List<LeaveRequest> studentLeaves(@PathVariable Long studentId) {
        return service.studentLeaves(studentId);
    }

    @GetMapping("/leaves/pending")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN','HOD','FACULTY')")
    @Operation(summary = "Pending leave requests for review")
    public List<LeaveRequest> pendingLeaves() {
        return service.pendingLeaves();
    }

    @PostMapping("/leaves/{id}/review")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN','HOD','FACULTY')")
    @Operation(summary = "Approve or reject a leave request")
    public LeaveRequest reviewLeave(@PathVariable Long id, @RequestParam String decision) {
        return service.reviewLeave(id, decision);
    }

    // ---- Document requests ----
    @PostMapping("/documents")
    @PreAuthorize("hasAnyRole('STUDENT','INSTITUTION_ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Request a document (transcript, bonafide, etc.)")
    public DocumentRequest createDocRequest(@Valid @RequestBody DocumentRequestDto req) {
        return service.createDocumentRequest(req);
    }

    @GetMapping("/documents/by-student/{studentId}")
    public List<DocumentRequest> studentDocuments(@PathVariable Long studentId) {
        return service.studentDocuments(studentId);
    }

    @GetMapping("/documents/pending")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN','HOD')")
    public List<DocumentRequest> pendingDocuments() {
        return service.pendingDocuments();
    }

    @PatchMapping("/documents/{id}/status")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN','HOD')")
    @Operation(summary = "Update document request status (PROCESSING → READY → COLLECTED)")
    public DocumentRequest updateDocStatus(@PathVariable Long id, @RequestParam String status) {
        return service.updateDocumentStatus(id, status);
    }
}
