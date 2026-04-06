package com.silaipro.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class RoleResponse {
    private Long id;
    private String name;
    private List<String> permissions;
    private LocalDateTime createdAt;
}
