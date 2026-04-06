package com.silaipro.controller;

import com.silaipro.dto.user.RoleRequest;
import com.silaipro.dto.user.RoleResponse;
import com.silaipro.security.HasPermission;
import com.silaipro.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@Tag(name = "Staff", description = "Role and permission management")
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    @HasPermission("STAFF_VIEW")
    @Operation(summary = "Get all roles")
    public List<RoleResponse> getAllRoles() {
        return roleService.getAllRoles();
    }

    @PostMapping
    @HasPermission("ADMIN_ONLY")
    @Operation(summary = "Create a new role (Admin Only)")
    public RoleResponse createRole(@Valid @RequestBody RoleRequest request) {
        return roleService.createRole(request);
    }

    @PutMapping("/{id}")
    @HasPermission("ADMIN_ONLY")
    @Operation(summary = "Update role name and permissions (Admin Only)")
    public RoleResponse updateRole(@PathVariable Long id, @Valid @RequestBody RoleRequest request) {
        return roleService.updateRole(id, request);
    }

    @PutMapping("/{id}/permissions")
    @HasPermission("ADMIN_ONLY")
    @Operation(summary = "Update specifically role permissions (Admin Only)")
    public RoleResponse updatePermissions(@PathVariable Long id, @RequestBody List<String> permissions) {
        return roleService.updatePermissions(id, permissions);
    }
}
