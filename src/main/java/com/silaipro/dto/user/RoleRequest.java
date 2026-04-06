package com.silaipro.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class RoleRequest {
    @NotBlank(message = "Role name is required")
    private String name;

    @NotEmpty(message = "Permissions list cannot be empty")
    private List<String> permissions;
}
