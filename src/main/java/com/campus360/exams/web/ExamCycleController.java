package com.campus360.exams.web;

import com.campus360.exams.domain.ExamComponent;
import com.campus360.exams.domain.ExamCycle;
import com.campus360.exams.domain.ExamSchedule;
import com.campus360.exams.service.ExamCycleService;
import com.campus360.exams.web.dto.CreateExamComponentRequest;
import com.campus360.exams.web.dto.CreateExamCycleRequest;
import com.campus360.exams.web.dto.CreateExamScheduleRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/exams/cycles")
public class ExamCycleController {

    private final ExamCycleService service;

    public ExamCycleController(ExamCycleService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN', 'EXAM_CONTROLLER')")
    public ExamCycle createCycle(@Valid @RequestBody CreateExamCycleRequest req) {
        return service.createCycle(req);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<ExamCycle> listCycles() {
        return service.listCycles();
    }

    @PostMapping("/{cycleId}/components")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN', 'EXAM_CONTROLLER')")
    public ExamComponent addComponent(@PathVariable Long cycleId, @Valid @RequestBody CreateExamComponentRequest req) {
        return service.addComponent(cycleId, req);
    }

    @GetMapping("/{cycleId}/courses/{courseId}/components")
    @PreAuthorize("isAuthenticated()")
    public List<ExamComponent> getComponents(@PathVariable Long cycleId, @PathVariable Long courseId) {
        return service.getComponents(cycleId, courseId);
    }

    @PostMapping("/{cycleId}/schedules")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN', 'EXAM_CONTROLLER')")
    public ExamSchedule addSchedule(@PathVariable Long cycleId, @Valid @RequestBody CreateExamScheduleRequest req) {
        return service.addSchedule(cycleId, req);
    }
}
