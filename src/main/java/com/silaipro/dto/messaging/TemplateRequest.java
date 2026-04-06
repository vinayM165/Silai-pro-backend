package com.silaipro.dto.messaging;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class TemplateRequest {
    @NotBlank(message = "Template name is required")
    private String name;

    @NotBlank(message = "Template content is required")
    private String content;

    private Boolean isActive;
}
