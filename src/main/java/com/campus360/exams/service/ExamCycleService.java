package com.campus360.exams.service;

import com.campus360.exams.domain.ExamComponent;
import com.campus360.exams.domain.ExamCycle;
import com.campus360.exams.domain.ExamSchedule;
import com.campus360.exams.repository.ExamComponentRepository;
import com.campus360.exams.repository.ExamCycleRepository;
import com.campus360.exams.repository.ExamScheduleRepository;
import com.campus360.exams.web.dto.CreateExamComponentRequest;
import com.campus360.exams.web.dto.CreateExamCycleRequest;
import com.campus360.exams.web.dto.CreateExamScheduleRequest;
import com.campus360.platform.audit.AuditService;
import com.campus360.platform.error.ApiException;
import com.campus360.platform.tenancy.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ExamCycleService {

    private final ExamCycleRepository cycleRepo;
    private final ExamComponentRepository compRepo;
    private final ExamScheduleRepository scheduleRepo;
    private final AuditService auditService;

    public ExamCycleService(ExamCycleRepository cycleRepo,
                            ExamComponentRepository compRepo,
                            ExamScheduleRepository scheduleRepo,
                            AuditService auditService) {
        this.cycleRepo = cycleRepo;
        this.compRepo = compRepo;
        this.scheduleRepo = scheduleRepo;
        this.auditService = auditService;
    }

    public ExamCycle createCycle(CreateExamCycleRequest req) {
        Long tenantId = TenantContext.requireTenantId();
        if (req.endDate().isBefore(req.startDate())) {
            throw ApiException.badRequest("End date cannot be before start date");
        }

        ExamCycle c = new ExamCycle();
        c.setTenantId(tenantId);
        c.setName(req.name());
        c.setTermId(req.termId());
        c.setAcademicYear(req.academicYear());
        c.setStartDate(req.startDate());
        c.setEndDate(req.endDate());
        c = cycleRepo.save(c);

        auditService.log("EXAM_CYCLE_CREATED", "ExamCycle", c.getId(), "Created exam cycle: " + req.name());
        return c;
    }

    @Transactional(readOnly = true)
    public List<ExamCycle> listCycles() {
        Long tenantId = TenantContext.requireTenantId();
        return cycleRepo.findByTenantIdOrderByStartDateDesc(tenantId);
    }

    public ExamComponent addComponent(Long cycleId, CreateExamComponentRequest req) {
        Long tenantId = TenantContext.requireTenantId();
        cycleRepo.findByIdAndTenantId(cycleId, tenantId)
                .orElseThrow(() -> ApiException.notFound("Exam cycle not found"));

        ExamComponent comp = new ExamComponent();
        comp.setTenantId(tenantId);
        comp.setExamCycleId(cycleId);
        comp.setCourseId(req.courseId());
        comp.setComponentName(req.componentName().toUpperCase());
        comp.setMaxMarks(req.maxMarks());
        comp.setPassingMarks(req.passingMarks());
        if (req.weightagePct() != null) comp.setWeightagePct(req.weightagePct());
        comp = compRepo.save(comp);

        auditService.log("EXAM_COMPONENT_ADDED", "ExamComponent", comp.getId(), "Added component " + req.componentName() + " to cycle " + cycleId);
        return comp;
    }

    public ExamSchedule addSchedule(Long cycleId, CreateExamScheduleRequest req) {
        Long tenantId = TenantContext.requireTenantId();
        cycleRepo.findByIdAndTenantId(cycleId, tenantId)
                .orElseThrow(() -> ApiException.notFound("Exam cycle not found"));

        ExamSchedule sch = new ExamSchedule();
        sch.setTenantId(tenantId);
        sch.setExamCycleId(cycleId);
        sch.setCourseId(req.courseId());
        sch.setExamDate(req.examDate());
        sch.setStartTime(req.startTime());
        sch.setEndTime(req.endTime());
        sch.setRoomId(req.roomId());
        sch.setInvigilatorId(req.invigilatorId());
        sch = scheduleRepo.save(sch);

        auditService.log("EXAM_SCHEDULE_ADDED", "ExamSchedule", sch.getId(), "Added schedule for course " + req.courseId() + " in cycle " + cycleId);
        return sch;
    }

    @Transactional(readOnly = true)
    public List<ExamComponent> getComponents(Long cycleId, Long courseId) {
        Long tenantId = TenantContext.requireTenantId();
        return compRepo.findByTenantIdAndExamCycleIdAndCourseId(tenantId, cycleId, courseId);
    }
}
