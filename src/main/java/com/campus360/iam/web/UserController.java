package com.campus360.iam.web;

import com.campus360.iam.domain.User;
import com.campus360.iam.service.UserManagementService;
import com.campus360.iam.web.dto.*;
import com.campus360.shared.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Enterprise user management endpoints. Only INSTITUTION_ADMIN (and HOD for
 * listing) can manage users within their tenant.
 */
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "User Management", description = "CRUD, role assignment, suspend/activate users within a tenant")
public class UserController {

    private final UserManagementService service;

    public UserController(UserManagementService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN','HOD')")
    @Operation(summary = "List users in the current tenant (paginated)")
    public PageResponse<User> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String role) {
        var pageable = PageRequest.of(page, Math.min(size, 100), Sort.by("fullName"));
        if (role != null && !role.isBlank()) {
            return PageResponse.of(service.listUsersByRole(role, pageable));
        }
        return PageResponse.of(service.listUsers(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('INSTITUTION_ADMIN','HOD')")
    public User get(@PathVariable Long id) {
        return service.getUser(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('INSTITUTION_ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new user (auto-generates password if omitted and emails it)")
    public User create(@Valid @RequestBody CreateUserRequest req) {
        return service.createUser(req);
    }

    /**
     * Bulk-create users in one request. Each row is processed independently:
     * failure on one row does NOT block the rest. Returns a per-row result
     * array so the UI can show a granular outcome table.
     *
     * <p>On completion, a tenant-wide in-app notification is dispatched
     * summarising the batch result.
     */
    @PostMapping("/bulk")
    @PreAuthorize("hasRole('INSTITUTION_ADMIN')")
    @ResponseStatus(HttpStatus.MULTI_STATUS)
    @Operation(summary = "Bulk create users — returns per-row success/failure, fires notifications")
    public List<BulkCreateUserResult> bulkCreate(@Valid @RequestBody BulkCreateUsersRequest req) {
        return service.bulkCreateUsers(req.users());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('INSTITUTION_ADMIN')")
    @Operation(summary = "Update user name, email, or status")
    public User update(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest req) {
        return service.updateUser(id, req);
    }

    @PutMapping("/{id}/roles")
    @PreAuthorize("hasRole('INSTITUTION_ADMIN')")
    @Operation(summary = "Replace the user's roles")
    public User assignRoles(@PathVariable Long id, @Valid @RequestBody RoleAssignmentRequest req) {
        return service.assignRoles(id, req.roles());
    }

    @PostMapping("/{id}/suspend")
    @PreAuthorize("hasRole('INSTITUTION_ADMIN')")
    @Operation(summary = "Suspend a user — they can no longer sign in")
    public User suspend(@PathVariable Long id) {
        return service.suspendUser(id);
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("hasRole('INSTITUTION_ADMIN')")
    public User activate(@PathVariable Long id) {
        return service.activateUser(id);
    }
}
