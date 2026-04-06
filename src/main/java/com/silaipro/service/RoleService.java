package com.silaipro.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.silaipro.dto.user.RoleRequest;
import com.silaipro.dto.user.RoleResponse;
import com.silaipro.entity.Role;
import com.silaipro.repository.RoleRepository;
import com.silaipro.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final ObjectMapper objectMapper;
    private final AuditLogService auditLogService;

    public List<RoleResponse> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public RoleResponse createRole(RoleRequest request) {
        if (roleRepository.findByName(request.getName()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role already exists");
        }

        Role role = Role.builder()
                .name(request.getName())
                .permissionsJson(serializePermissions(request.getPermissions()))
                .build();

        role = roleRepository.save(role);
        return mapToResponse(role);
    }

    @Transactional
    public RoleResponse updateRole(Long id, RoleRequest request) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found"));

        role.setName(request.getName());
        role.setPermissionsJson(serializePermissions(request.getPermissions()));

        role = roleRepository.save(role);
        return mapToResponse(role);
    }

    @Transactional
    public RoleResponse updatePermissions(Long id, List<String> permissions) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found"));

        String oldVal = role.getPermissionsJson();
        String newVal = serializePermissions(permissions);
        
        role.setPermissionsJson(newVal);
        role = roleRepository.save(role);

        auditLogService.log("PERMISSIONS_CHANGE", "ROLE", id, oldVal, newVal);
        
        return mapToResponse(role);
    }

    private String serializePermissions(List<String> permissions) {
        try {
            return objectMapper.writeValueAsString(permissions != null ? permissions : Collections.emptyList());
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error serializing permissions");
        }
    }

    private List<String> deserializePermissions(String json) {
        if (json == null || json.isEmpty()) return Collections.emptyList();
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            return Collections.emptyList();
        }
    }

    private RoleResponse mapToResponse(Role role) {
        return RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .permissions(deserializePermissions(role.getPermissionsJson()))
                .createdAt(role.getCreatedAt())
                .build();
    }
}
