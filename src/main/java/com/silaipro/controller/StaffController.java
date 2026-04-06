package com.silaipro.controller;

import com.silaipro.dto.user.*;
import com.silaipro.entity.AuditLog;
import com.silaipro.security.HasPermission;
import com.silaipro.service.StaffService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Staff", description = "Staff and employee management")
public class StaffController {

    private final StaffService staffService;

    @GetMapping("/api/users")
    @HasPermission("ADMIN")
    @Operation(summary = "Get paginated list of staff")
    public Page<StaffResponse> getAllStaff(Pageable pageable) {
        return staffService.getAllStaff(pageable);
    }

    @PostMapping("/api/users")
    @HasPermission("ADMIN")
    @Operation(summary = "Create a new staff member")
    public StaffResponse createStaff(@Valid @RequestBody StaffRequest request) {
        return staffService.createStaff(request);
    }

    @PutMapping("/api/users/{id}")
    @HasPermission("ADMIN")
    @Operation(summary = "Update staff details")
    public StaffResponse updateStaff(@PathVariable Long id, @Valid @RequestBody StaffRequest request) {
        return staffService.updateStaff(id, request);
    }

    @PatchMapping("/api/users/{id}/deactivate")
    @HasPermission("ADMIN")
    @Operation(summary = "Deactivate staff member")
    public void deactivateStaff(@PathVariable Long id) {
        staffService.deactivateStaff(id);
    }

    @PatchMapping("/api/users/{id}/reset-password")
    @HasPermission("ADMIN")
    @Operation(summary = "Reset staff password (Admin Only)")
    public void resetPassword(@PathVariable Long id, @Valid @RequestBody PasswordResetRequest request) {
        staffService.resetPassword(id, request);
    }

    @PatchMapping("/api/users/{id}/role")
    @HasPermission("ADMIN")
    @Operation(summary = "Assign a new role to staff")
    public void assignRole(@PathVariable Long id, @RequestParam Long roleId) {
        staffService.assignRole(id, roleId);
    }

    @GetMapping("/api/users/{id}/login-history")
    @HasPermission("ADMIN")
    @Operation(summary = "Get staff login history")
    public List<AuditLog> getLoginHistory(@PathVariable Long id) {
        return staffService.getLoginHistory(id);
    }

    @GetMapping("/api/users/{id}/work-report")
    @HasPermission("ADMIN")
    @Operation(summary = "Get staff individual work report")
    public StaffWorkReport getWorkReport(
            @PathVariable Long id,
            @RequestParam LocalDate from,
            @RequestParam LocalDate to) {
        return staffService.getStaffWorkReport(id, from, to);
    }
}
