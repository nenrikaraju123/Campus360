package com.campus360.institution.web;

import com.campus360.institution.domain.*;
import com.campus360.institution.service.AcademicStructureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Academic Structure", description = "Departments, programs, courses, terms, sections — full CRUD")
public class AcademicStructureController {

    private final AcademicStructureService service;

    public AcademicStructureController(AcademicStructureService service) {
        this.service = service;
    }

    // ---- Departments ----
    @PostMapping("/departments")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN','HOD')")
    @ResponseStatus(HttpStatus.CREATED)
    public Department createDepartment(@Valid @RequestBody DepartmentRequest req) {
        return service.createDepartment(req);
    }

    @PutMapping("/departments/{id}")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN','HOD')")
    @Operation(summary = "Update department name or HOD")
    public Department updateDepartment(@PathVariable Long id, @Valid @RequestBody DepartmentRequest req) {
        return service.updateDepartment(id, req);
    }

    @DeleteMapping("/departments/{id}")
    @PreAuthorize("hasRole('INSTITUTION_ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDepartment(@PathVariable Long id) {
        service.deleteDepartment(id);
    }

    @GetMapping("/departments")
    public List<Department> departments() {
        return service.listDepartments();
    }

    @GetMapping("/departments/{id}")
    public Department department(@PathVariable Long id) {
        return service.getDepartment(id);
    }

    // ---- Programs ----
    @PostMapping("/programs")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN','HOD')")
    @ResponseStatus(HttpStatus.CREATED)
    public Program createProgram(@Valid @RequestBody ProgramRequest req) {
        return service.createProgram(req);
    }

    @PutMapping("/programs/{id}")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN','HOD')")
    public Program updateProgram(@PathVariable Long id, @Valid @RequestBody ProgramRequest req) {
        return service.updateProgram(id, req);
    }

    @DeleteMapping("/programs/{id}")
    @PreAuthorize("hasRole('INSTITUTION_ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProgram(@PathVariable Long id) {
        service.deleteProgram(id);
    }

    @GetMapping("/programs")
    public List<Program> programs(@RequestParam(required = false) Long departmentId) {
        return departmentId != null ? service.listProgramsByDepartment(departmentId)
                : service.listPrograms();
    }

    @GetMapping("/programs/{id}")
    public Program program(@PathVariable Long id) {
        return service.getProgram(id);
    }

    // ---- Courses ----
    @PostMapping("/courses")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN','HOD','FACULTY')")
    @ResponseStatus(HttpStatus.CREATED)
    public Course createCourse(@Valid @RequestBody CourseRequest req) {
        return service.createCourse(req);
    }

    @PutMapping("/courses/{id}")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN','HOD','FACULTY')")
    public Course updateCourse(@PathVariable Long id, @Valid @RequestBody CourseRequest req) {
        return service.updateCourse(id, req);
    }

    @DeleteMapping("/courses/{id}")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN','HOD')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCourse(@PathVariable Long id) {
        service.deleteCourse(id);
    }

    @GetMapping("/courses")
    public List<Course> courses(@RequestParam(required = false) Long departmentId) {
        return departmentId != null ? service.listCoursesByDepartment(departmentId)
                : service.listCourses();
    }

    @GetMapping("/courses/{id}")
    public Course course(@PathVariable Long id) {
        return service.getCourse(id);
    }

    // ---- Terms ----
    @PostMapping("/terms")
    @PreAuthorize("hasRole('INSTITUTION_ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public AcademicTerm createTerm(@Valid @RequestBody TermRequest req) {
        return service.createTerm(req);
    }

    @PutMapping("/terms/{id}")
    @PreAuthorize("hasRole('INSTITUTION_ADMIN')")
    public AcademicTerm updateTerm(@PathVariable Long id, @Valid @RequestBody TermRequest req) {
        return service.updateTerm(id, req);
    }

    @DeleteMapping("/terms/{id}")
    @PreAuthorize("hasRole('INSTITUTION_ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTerm(@PathVariable Long id) {
        service.deleteTerm(id);
    }

    @GetMapping("/terms")
    public List<AcademicTerm> terms() {
        return service.listTerms();
    }

    @GetMapping("/terms/{id}")
    public AcademicTerm term(@PathVariable Long id) {
        return service.getTerm(id);
    }

    // ---- Sections ----
    @PostMapping("/sections")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN','HOD')")
    @ResponseStatus(HttpStatus.CREATED)
    public Section createSection(@Valid @RequestBody SectionRequest req) {
        return service.createSection(req);
    }

    @PutMapping("/sections/{id}")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN','HOD')")
    public Section updateSection(@PathVariable Long id, @Valid @RequestBody SectionRequest req) {
        return service.updateSection(id, req);
    }

    @DeleteMapping("/sections/{id}")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN','HOD')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSection(@PathVariable Long id) {
        service.deleteSection(id);
    }

    @GetMapping("/sections")
    public List<Section> sections(@RequestParam(required = false) Long termId) {
        return service.listSections(termId);
    }

    @GetMapping("/sections/{id}")
    public Section section(@PathVariable Long id) {
        return service.getSection(id);
    }

    // ---- Curriculum Items ----
    @PostMapping("/curriculum-items")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN','HOD')")
    @ResponseStatus(HttpStatus.CREATED)
    public CurriculumItem addCurriculumItem(@Valid @RequestBody CurriculumItem item) {
        return service.addCurriculumItem(item);
    }

    @GetMapping("/curriculum-items")
    public List<CurriculumItem> getCurriculum(@RequestParam Long programId) {
        return service.getCurriculumForProgram(programId);
    }

    @DeleteMapping("/curriculum-items/{id}")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN','HOD')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCurriculumItem(@PathVariable Long id) {
        service.removeCurriculumItem(id);
    }
}
