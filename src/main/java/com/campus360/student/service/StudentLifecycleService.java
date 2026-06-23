package com.campus360.student.service;

import com.campus360.platform.audit.AuditService;
import com.campus360.platform.error.ApiException;
import com.campus360.platform.security.CurrentUser;
import com.campus360.platform.tenancy.TenantContext;
import com.campus360.student.domain.StudentLifecycleHistory;
import com.campus360.student.domain.StudentProfile;
import com.campus360.student.repository.StudentLifecycleHistoryRepository;
import com.campus360.student.repository.StudentProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Manages student lifecycle state transitions:
 * ACTIVE → PROMOTED / TRANSFERRED / SUSPENDED / GRADUATED / ARCHIVED / WITHDRAWN
 *
 * Every transition writes a status history record and an audit log.
 */
@Service
@Transactional
public class StudentLifecycleService {

    private static final Map<String, Set<String>> TRANSITIONS = Map.of(
            "ACTIVE",       Set.of("SUSPENDED", "GRADUATED", "TRANSFERRED", "WITHDRAWN", "ARCHIVED"),
            "SUSPENDED",    Set.of("ACTIVE", "WITHDRAWN", "ARCHIVED"),
            "TRANSFERRED",  Set.of("ACTIVE"),
            "GRADUATED",    Set.of("ARCHIVED"),
            "WITHDRAWN",    Set.of("ARCHIVED"),
            "ARCHIVED",     Set.of()
    );

    private final StudentProfileRepository profileRepository;
    private final StudentLifecycleHistoryRepository historyRepository;
    private final AuditService auditService;

    public StudentLifecycleService(StudentProfileRepository profileRepository,
                                   StudentLifecycleHistoryRepository historyRepository,
                                   AuditService auditService) {
        this.profileRepository = profileRepository;
        this.historyRepository = historyRepository;
        this.auditService = auditService;
    }

    public StudentProfile transition(Long studentId, String action, String targetStatus, String comment) {
        Long tenantId = TenantContext.requireTenantId();
        StudentProfile student = profileRepository.findByIdAndTenantId(studentId, tenantId)
                .orElseThrow(() -> ApiException.notFound("Student not found: " + studentId));

        String fromStatus = student.getLifecycleStatus();
        Set<String> allowed = TRANSITIONS.getOrDefault(fromStatus, Set.of());
        if (!allowed.contains(targetStatus)) {
            throw ApiException.badRequest(
                    String.format("Cannot transition student from '%s' to '%s'", fromStatus, targetStatus));
        }

        student.setLifecycleStatus(targetStatus);
        profileRepository.save(student);

        // Write history
        StudentLifecycleHistory history = new StudentLifecycleHistory();
        history.setTenantId(tenantId);
        history.setStudentId(studentId);
        history.setFromStatus(fromStatus);
        history.setToStatus(targetStatus);
        history.setAction(action);
        history.setComment(comment);
        history.setActorId(CurrentUser.principal() != null ? CurrentUser.principal().userId() : null);
        historyRepository.save(history);

        // Audit log
        auditService.log("STUDENT_LIFECYCLE_" + action, "StudentProfile", studentId,
                String.format("%s → %s: %s", fromStatus, targetStatus,
                        comment != null ? comment : ""));

        return student;
    }

    public StudentProfile promote(Long studentId, String comment) {
        Long tenantId = TenantContext.requireTenantId();
        StudentProfile student = profileRepository.findByIdAndTenantId(studentId, tenantId)
                .orElseThrow(() -> ApiException.notFound("Student not found: " + studentId));

        if (!"ACTIVE".equals(student.getLifecycleStatus())) {
            throw ApiException.badRequest("Only ACTIVE students can be promoted");
        }
        student.setCurrentTerm(student.getCurrentTerm() + 1);
        profileRepository.save(student);

        // Write history
        StudentLifecycleHistory history = new StudentLifecycleHistory();
        history.setTenantId(tenantId);
        history.setStudentId(studentId);
        history.setFromStatus("ACTIVE");
        history.setToStatus("ACTIVE");
        history.setAction("PROMOTED");
        history.setComment(comment != null ? comment : "Term " + (student.getCurrentTerm() - 1) + " → " + student.getCurrentTerm());
        history.setActorId(CurrentUser.principal() != null ? CurrentUser.principal().userId() : null);
        historyRepository.save(history);

        auditService.log("STUDENT_PROMOTED", "StudentProfile", studentId,
                "Promoted to term " + student.getCurrentTerm());
        return student;
    }

    @Transactional(readOnly = true)
    public List<StudentLifecycleHistory> getHistory(Long studentId) {
        Long tenantId = TenantContext.requireTenantId();
        profileRepository.findByIdAndTenantId(studentId, tenantId)
                .orElseThrow(() -> ApiException.notFound("Student not found: " + studentId));
        return historyRepository.findByStudentIdOrderByCreatedAtDesc(studentId);
    }
}
