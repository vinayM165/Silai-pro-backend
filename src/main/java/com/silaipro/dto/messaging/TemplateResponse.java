package com.silaipro.dto.messaging;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class TemplateResponse {
    private Long id;
    private String name;
    private String content;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
