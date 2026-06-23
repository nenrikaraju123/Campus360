package com.campus360.exams.service;

import com.campus360.exams.domain.ExamMark;
import com.campus360.exams.domain.ExamMarkSheet;
import com.campus360.exams.repository.ExamMarkRepository;
import com.campus360.exams.repository.ExamMarkSheetRepository;
import com.campus360.exams.web.dto.CreateExamMarkSheetRequest;
import com.campus360.exams.web.dto.UpdateExamMarksRequest;
import com.campus360.faculty.domain.FacultyCourseAssignment;
import com.campus360.faculty.repository.FacultyCourseAssignmentRepository;
import com.campus360.platform.audit.AuditService;
import com.campus360.platform.error.ApiException;
import com.campus360.platform.tenancy.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class ExamMarkSheetService {

    private final ExamMarkSheetRepository markSheetRepo;
    private final ExamMarkRepository markRepo;
    private final FacultyCourseAssignmentRepository assignmentRepo;
    private final AuditService auditService;

    public ExamMarkSheetService(ExamMarkSheetRepository markSheetRepo,
                                ExamMarkRepository markRepo,
                                FacultyCourseAssignmentRepository assignmentRepo,
                                AuditService auditService) {
        this.markSheetRepo = markSheetRepo;
        this.markRepo = markRepo;
        this.assignmentRepo = assignmentRepo;
        this.auditService = auditService;
    }

    public ExamMarkSheet createMarkSheet(Long examCycleId, CreateExamMarkSheetRequest req) {
        Long tenantId = TenantContext.requireTenantId();

        // Authorize: faculty must be assigned to this section/course
        boolean isAssigned = assignmentRepo.existsByTenantIdAndFacultyIdAndSectionId(
                tenantId, req.facultyId(), req.sectionId());
        if (!isAssigned) {
            throw ApiException.badRequest("Faculty is not assigned to this course/section");
        }

        if (markSheetRepo.findByTenantIdAndExamCycleIdAndCourseIdAndSectionId(
                tenantId, examCycleId, req.courseId(), req.sectionId()).isPresent()) {
            throw ApiException.conflict("Mark sheet already exists for this cycle, course, and section");
        }

        ExamMarkSheet ms = new ExamMarkSheet();
        ms.setTenantId(tenantId);
        ms.setExamCycleId(examCycleId);
        ms.setCourseId(req.courseId());
        ms.setSectionId(req.sectionId());
        ms.setFacultyId(req.facultyId());
        ms = markSheetRepo.save(ms);

        auditService.log("MARK_SHEET_CREATED", "ExamMarkSheet", ms.getId(), "Created mark sheet for section " + req.sectionId());
        return ms;
    }

    public void updateMarks(Long markSheetId, UpdateExamMarksRequest req) {
        Long tenantId = TenantContext.requireTenantId();
        ExamMarkSheet ms = markSheetRepo.findByIdAndTenantId(markSheetId, tenantId)
                .orElseThrow(() -> ApiException.notFound("Mark sheet not found"));

        if (!"DRAFT".equals(ms.getStatus())) {
            throw ApiException.badRequest("Cannot update marks when mark sheet is in " + ms.getStatus() + " status");
        }

        for (UpdateExamMarksRequest.ExamMarkDto dto : req.marks()) {
            ExamMark mark = markRepo.findByTenantIdAndMarkSheetIdAndStudentIdAndExamComponentId(
                    tenantId, markSheetId, dto.studentId(), dto.examComponentId())
                    .orElseGet(() -> {
                        ExamMark nm = new ExamMark();
                        nm.setTenantId(tenantId);
                        nm.setMarkSheetId(markSheetId);
                        nm.setStudentId(dto.studentId());
                        nm.setExamComponentId(dto.examComponentId());
                        return nm;
                    });

            mark.setMarksObtained(dto.marksObtained());
            if (dto.isAbsent() != null) mark.setIsAbsent(dto.isAbsent());
            if (dto.remarks() != null) mark.setRemarks(dto.remarks());
            mark.setUpdatedAt(Instant.now());
            markRepo.save(mark);
        }

        ms.setUpdatedAt(Instant.now());
        markSheetRepo.save(ms);

        auditService.log("MARKS_UPDATED", "ExamMarkSheet", markSheetId, "Updated " + req.marks().size() + " marks");
    }

    @Transactional(readOnly = true)
    public List<ExamMark> getMarks(Long markSheetId) {
        Long tenantId = TenantContext.requireTenantId();
        return markRepo.findByTenantIdAndMarkSheetId(tenantId, markSheetId);
    }
}
