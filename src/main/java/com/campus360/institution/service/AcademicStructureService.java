package com.campus360.institution.service;

import com.campus360.institution.domain.*;
import com.campus360.institution.repository.*;
import com.campus360.institution.web.*;
import com.campus360.platform.error.ApiException;
import com.campus360.platform.tenancy.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Owns the academic backbone: departments, programs, courses, terms, sections.
 * Every operation is scoped to the caller's tenant via {@link TenantContext}.
 */
@Service
@Transactional
public class AcademicStructureService {

    private final DepartmentRepository departments;
    private final ProgramRepository programs;
    private final CourseRepository courses;
    private final AcademicTermRepository terms;
    private final SectionRepository sections;
    private final CurriculumItemRepository curriculumItems;

    public AcademicStructureService(DepartmentRepository departments, ProgramRepository programs,
                                    CourseRepository courses, AcademicTermRepository terms,
                                    SectionRepository sections, CurriculumItemRepository curriculumItems) {
        this.departments = departments;
        this.programs = programs;
        this.courses = courses;
        this.terms = terms;
        this.sections = sections;
        this.curriculumItems = curriculumItems;
    }

    // ---- Departments ----
    public Department createDepartment(DepartmentRequest req) {
        Long tenant = TenantContext.requireTenantId();
        if (departments.existsByTenantIdAndCodeIgnoreCase(tenant, req.code())) {
            throw ApiException.conflict("Department code already exists: " + req.code());
        }
        Department d = new Department();
        d.setTenantId(tenant);
        d.setName(req.name());
        d.setCode(req.code());
        d.setHodUserId(req.hodUserId());
        return departments.save(d);
    }

    public Department updateDepartment(Long id, DepartmentRequest req) {
        Department d = getDepartment(id);
        d.setName(req.name());
        if (req.hodUserId() != null) d.setHodUserId(req.hodUserId());
        return departments.save(d);
    }

    public void deleteDepartment(Long id) {
        Department d = getDepartment(id);
        departments.delete(d);
    }

    public List<Department> listDepartments() {
        return departments.findByTenantId(TenantContext.requireTenantId());
    }

    public Department getDepartment(Long id) {
        return departments.findByIdAndTenantId(id, TenantContext.requireTenantId())
                .orElseThrow(() -> ApiException.notFound("Department not found: " + id));
    }

    // ---- Programs ----
    public Program createProgram(ProgramRequest req) {
        Long tenant = TenantContext.requireTenantId();
        getDepartment(req.departmentId()); // validates ownership
        if (programs.existsByTenantIdAndCodeIgnoreCase(tenant, req.code())) {
            throw ApiException.conflict("Program code already exists: " + req.code());
        }
        Program p = new Program();
        p.setTenantId(tenant);
        p.setDepartmentId(req.departmentId());
        p.setName(req.name());
        p.setCode(req.code());
        if (req.level() != null) p.setLevel(req.level());
        if (req.durationTerms() > 0) p.setDurationTerms(req.durationTerms());
        if (req.totalCredits() > 0) p.setTotalCredits(req.totalCredits());
        return programs.save(p);
    }

    public Program updateProgram(Long id, ProgramRequest req) {
        Program p = getProgram(id);
        p.setName(req.name());
        if (req.level() != null) p.setLevel(req.level());
        if (req.durationTerms() > 0) p.setDurationTerms(req.durationTerms());
        if (req.totalCredits() > 0) p.setTotalCredits(req.totalCredits());
        return programs.save(p);
    }

    public void deleteProgram(Long id) {
        Program p = getProgram(id);
        programs.delete(p);
    }

    public List<Program> listPrograms() {
        return programs.findByTenantId(TenantContext.requireTenantId());
    }

    public List<Program> listProgramsByDepartment(Long departmentId) {
        return programs.findByTenantIdAndDepartmentId(TenantContext.requireTenantId(), departmentId);
    }

    public Program getProgram(Long id) {
        return programs.findByIdAndTenantId(id, TenantContext.requireTenantId())
                .orElseThrow(() -> ApiException.notFound("Program not found: " + id));
    }

    // ---- Courses ----
    public Course createCourse(CourseRequest req) {
        Long tenant = TenantContext.requireTenantId();
        getDepartment(req.departmentId());
        if (courses.existsByTenantIdAndCodeIgnoreCase(tenant, req.code())) {
            throw ApiException.conflict("Course code already exists: " + req.code());
        }
        Course c = new Course();
        c.setTenantId(tenant);
        c.setDepartmentId(req.departmentId());
        c.setCode(req.code());
        c.setTitle(req.title());
        if (req.creditHours() > 0) c.setCreditHours(req.creditHours());
        if (req.type() != null) c.setType(req.type());
        c.setDescription(req.description());
        return courses.save(c);
    }

    public Course updateCourse(Long id, CourseRequest req) {
        Course c = getCourse(id);
        c.setTitle(req.title());
        if (req.creditHours() > 0) c.setCreditHours(req.creditHours());
        if (req.type() != null) c.setType(req.type());
        c.setDescription(req.description());
        return courses.save(c);
    }

    public void deleteCourse(Long id) {
        Course c = getCourse(id);
        courses.delete(c);
    }

    public List<Course> listCourses() {
        return courses.findByTenantId(TenantContext.requireTenantId());
    }

    public List<Course> listCoursesByDepartment(Long departmentId) {
        return courses.findByTenantIdAndDepartmentId(TenantContext.requireTenantId(), departmentId);
    }

    public Course getCourse(Long id) {
        return courses.findByIdAndTenantId(id, TenantContext.requireTenantId())
                .orElseThrow(() -> ApiException.notFound("Course not found: " + id));
    }

    // ---- Terms ----
    public AcademicTerm createTerm(TermRequest req) {
        AcademicTerm t = new AcademicTerm();
        t.setTenantId(TenantContext.requireTenantId());
        t.setName(req.name());
        t.setStartDate(req.startDate());
        t.setEndDate(req.endDate());
        t.setAddDropEnd(req.addDropEnd());
        if (req.status() != null) t.setStatus(req.status());
        return terms.save(t);
    }

    public AcademicTerm updateTerm(Long id, TermRequest req) {
        AcademicTerm t = getTerm(id);
        t.setName(req.name());
        if (req.startDate() != null) t.setStartDate(req.startDate());
        if (req.endDate() != null) t.setEndDate(req.endDate());
        if (req.addDropEnd() != null) t.setAddDropEnd(req.addDropEnd());
        if (req.status() != null) t.setStatus(req.status());
        return terms.save(t);
    }

    public void deleteTerm(Long id) {
        AcademicTerm t = getTerm(id);
        terms.delete(t);
    }

    public AcademicTerm getTerm(Long id) {
        return terms.findByIdAndTenantId(id, TenantContext.requireTenantId())
                .orElseThrow(() -> ApiException.notFound("Term not found: " + id));
    }

    public List<AcademicTerm> listTerms() {
        return terms.findByTenantId(TenantContext.requireTenantId());
    }

    // ---- Sections ----
    public Section createSection(SectionRequest req) {
        Long tenant = TenantContext.requireTenantId();
        courses.findByIdAndTenantId(req.courseId(), tenant)
                .orElseThrow(() -> ApiException.badRequest("Unknown course: " + req.courseId()));
        terms.findByIdAndTenantId(req.termId(), tenant)
                .orElseThrow(() -> ApiException.badRequest("Unknown term: " + req.termId()));
        Section s = new Section();
        s.setTenantId(tenant);
        s.setCourseId(req.courseId());
        s.setTermId(req.termId());
        s.setFacultyUserId(req.facultyUserId());
        if (req.capacity() > 0) s.setCapacity(req.capacity());
        s.setSchedule(req.schedule());
        return sections.save(s);
    }

    public Section updateSection(Long id, SectionRequest req) {
        Section s = getSection(id);
        if (req.facultyUserId() != null) s.setFacultyUserId(req.facultyUserId());
        if (req.capacity() > 0) s.setCapacity(req.capacity());
        if (req.schedule() != null) s.setSchedule(req.schedule());
        return sections.save(s);
    }

    public void deleteSection(Long id) {
        Section s = getSection(id);
        sections.delete(s);
    }

    public Section getSection(Long id) {
        return sections.findByIdAndTenantId(id, TenantContext.requireTenantId())
                .orElseThrow(() -> ApiException.notFound("Section not found: " + id));
    }

    public List<Section> listSections(Long termId) {
        Long tenant = TenantContext.requireTenantId();
        return termId == null ? sections.findByTenantId(tenant)
                : sections.findByTenantIdAndTermId(tenant, termId);
    }

    // ---- Curriculum Items ----
    public CurriculumItem addCurriculumItem(CurriculumItem item) {
        Long tenant = TenantContext.requireTenantId();
        item.setTenantId(tenant);
        return curriculumItems.save(item);
    }

    public List<CurriculumItem> getCurriculumForProgram(Long programId) {
        return curriculumItems.findByTenantIdAndProgramId(TenantContext.requireTenantId(), programId);
    }

    public void removeCurriculumItem(Long id) {
        curriculumItems.findById(id).ifPresent(curriculumItems::delete);
    }
}
